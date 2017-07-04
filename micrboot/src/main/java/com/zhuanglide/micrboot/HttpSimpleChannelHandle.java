package com.zhuanglide.micrboot;

import com.zhuanglide.micrboot.constants.Constants;
import com.zhuanglide.micrboot.http.HttpContextRequest;
import com.zhuanglide.micrboot.http.HttpContextResponse;
import com.zhuanglide.micrboot.http.HttpHeaderName;
import com.zhuanglide.micrboot.mvc.ApiDispatcher;
import com.zhuanglide.micrboot.util.RequestIdGenerator;
import io.netty.channel.*;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.handler.codec.TooLongFrameException;
import io.netty.handler.codec.http.*;
import io.netty.handler.stream.ChunkedFile;
import io.netty.handler.stream.ChunkedStream;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.io.RandomAccessFile;

/**
 * HTTP handle
 */
@Sharable
public class HttpSimpleChannelHandle extends SimpleChannelInboundHandler<FullHttpRequest> implements ApplicationContextAware,InitializingBean{
    private Logger logger = LoggerFactory.getLogger(HttpSimpleChannelHandle.class);
    private ServerConfig serverConfig;
    private ApplicationContext context;
    private ApiDispatcher dispatcher;
    private ChannelFutureListener listener;
    /**
     * 初始化
     * @param context
     */
    protected void initStrategies(ApplicationContext context) {
        initApiMethodDispatcher(context);
        listener = new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if(!isKeepAlive(future.channel())) {
                    future.channel().close();
                }
                if(serverConfig.isOpenConnectCostLogger()) {
                    long reqId = future.channel().attr(Constants.ATTR_REQ_ID).get();
                    Long startTime = future.channel().attr(Constants.ATTR_CONN_ACTIVE_TIME).get();
                    logger.info("http finish,reqId={},cost={}ms", reqId, System.currentTimeMillis() - startTime);
                }
            }
        };
    }

    protected boolean isKeepAlive(Channel channel) {
        Boolean keepAlive = channel.attr(Constants.KEEP_ALIVE_KEY).get();
        keepAlive = keepAlive==null?false:keepAlive;
        return keepAlive;
    }

    /**
     * 初始化 ApiMethodDispatcher
     * @param context
     */
    protected void initApiMethodDispatcher(ApplicationContext context){
        try {
            dispatcher = context.getBean(ApiDispatcher.class);
        }catch (NoSuchBeanDefinitionException e){
            dispatcher = context.getAutowireCapableBeanFactory() .createBean(ApiDispatcher.class);
            context.getAutowireCapableBeanFactory().autowireBean(dispatcher);
        }
        assert dispatcher != null;
    }

    @Override
    protected void channelRead0(final ChannelHandlerContext ctx, final FullHttpRequest fullRequest)
            throws Exception {
        if (fullRequest.decoderResult().isFailure()) {
            DefaultFullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST);
            sendResponse(ctx, response);
            return;
        }
        ctx.channel().attr(Constants.ATTR_REQ_ID).set(getReqId());
        ctx.channel().attr(Constants.ATTR_CONN_ACTIVE_TIME).set(System.currentTimeMillis());
        final HttpContextRequest request = new HttpContextRequest(fullRequest, getServerConfig().getCharset());
        ctx.executor().execute(new Runnable() {
            @Override
            public void run() {
                ctx.channel().attr(Constants.KEEP_ALIVE_KEY).set(HttpUtil.isKeepAlive(fullRequest));
                //转化为 api 能处理的request\response
                HttpContextResponse response = new HttpContextResponse(fullRequest.protocolVersion(), HttpResponseStatus.OK, getServerConfig().getCharset());
                HttpResponse httpResponse;
                try {
                    dispatcher.doService(request, response);
                    if(null == response){
                        httpResponse = new DefaultFullHttpResponse(fullRequest.protocolVersion(), HttpResponseStatus.BAD_REQUEST);
                    }else{
                        //文件
                        if (response.getFile() == null || !response.getFile().isFile()) {
                            DefaultFullHttpResponse fullHttpResponse = new DefaultFullHttpResponse(response.getVersion(), response.getStatus());
                            fullHttpResponse.headers().clear();
                            fullHttpResponse.headers().add(response.headers());
                            if(response.content()!=null) {
                                fullHttpResponse.content().writeBytes(response.content());
                            }
                            httpResponse = fullHttpResponse;
                        }else{
                            RandomAccessFile raf = new RandomAccessFile(response.getFile(), "r");
                            long fileLength = raf.length();
                            httpResponse = new DefaultHttpResponse(request.getHttpVersion(), response.getStatus());
                            httpResponse.headers().add(response.headers());
                            packageResponseHeader(httpResponse, fileLength, ctx);
                            ctx.write(response);
                            ctx.write(new HttpChunkedInput(new ChunkedFile(raf, 0, fileLength, serverConfig.getChunkSize())));
                            sendResponse(ctx,LastHttpContent.EMPTY_LAST_CONTENT);
                            return;
                        }
                    }
                } catch (Exception e) {
                    logger.error("", e);
                    httpResponse = new DefaultFullHttpResponse(fullRequest.protocolVersion(), HttpResponseStatus.INTERNAL_SERVER_ERROR);
                }
                sendResponse(ctx, httpResponse);
            }
        });
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("", cause);
        FullHttpResponse response;
        try {
            response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST);
            if (cause instanceof TooLongFrameException) {
                response.setStatus(HttpResponseStatus.BAD_REQUEST);
            }else {
                response.setStatus(HttpResponseStatus.INTERNAL_SERVER_ERROR);
            }
            String ex = cause.getMessage();
            if (null == ex) {
                ex = String.valueOf(cause);
            }
            response.content().writeBytes(ex.getBytes(getServerConfig().getCharset()));
        } catch (Exception e) {
            logger.error("", e);
            response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST);
        }
        sendResponse(ctx, response);
    }


    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if(evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            logger.warn("user event triggered ,event.state={},address={}", event.state(), ctx.channel().remoteAddress());
            ctx.close();
        }
        super.userEventTriggered(ctx,evt);
    }

    protected void packageFullResponseHeader(FullHttpResponse response,ChannelHandlerContext ctx){
        long len = 0;
        if (response.content() != null) {
            len = response.content().readableBytes();
        }
        packageResponseHeader(response, len, ctx);
    }

    protected void packageResponseHeader(HttpResponse response,long len,ChannelHandlerContext ctx) {
        if(serverConfig.getHeaderServer() != null
                && serverConfig.getHeaderServer().length()>0 &&
                !response.headers().contains(HttpHeaderName.SERVER)){
            response.headers().add(HttpHeaderName.SERVER, serverConfig.getHeaderServer());
        }
        if (!response.headers().contains(HttpHeaderName.CONTENT_LENGTH)) {
            response.headers().add(HttpHeaderName.CONTENT_LENGTH, len);
        }
        if(isKeepAlive(ctx.channel()) &&
                !response.headers().contains(HttpHeaderName.CONNECTION)){
            response.headers().add(HttpHeaderName.CONNECTION, Constants.KEEP_ALIVE);
        }
    }

    protected void sendResponse(ChannelHandlerContext ctx, HttpObject response) {
        if (response instanceof FullHttpResponse) {
            packageFullResponseHeader((FullHttpResponse) response, ctx);
        }
        ctx.writeAndFlush(response).addListener(listener);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        initStrategies(context);
    }


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
    }

    public ServerConfig getServerConfig() {
        return serverConfig;
    }

    public void setServerConfig(ServerConfig serverConfig) {
        this.serverConfig = serverConfig;
    }

    private Long getReqId(){
        return RequestIdGenerator.getRequestId();
    }
}
