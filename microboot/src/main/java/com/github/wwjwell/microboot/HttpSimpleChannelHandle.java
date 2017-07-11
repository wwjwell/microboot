package com.github.wwjwell.microboot;

import com.github.wwjwell.microboot.constants.Constants;
import com.github.wwjwell.microboot.http.HttpContextRequest;
import com.github.wwjwell.microboot.http.HttpContextResponse;
import com.github.wwjwell.microboot.http.HttpHeaderName;
import com.github.wwjwell.microboot.http.MediaType;
import com.github.wwjwell.microboot.mvc.ApiDispatcher;
import com.github.wwjwell.microboot.util.RequestIdGenerator;
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

import java.io.File;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicInteger;

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
            }
        };
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
        final HttpContextRequest request = prepareHttpRequest(ctx, fullRequest);
        ctx.executor().execute(new Runnable() {
            @Override
            public void run() {
                //转化为 api 能处理的request\response
                HttpContextResponse response = new HttpContextResponse(fullRequest.protocolVersion(), HttpResponseStatus.OK, getServerConfig().getCharset());
                HttpResponse httpResponse;
                try {
                    dispatcher.doService(request, response);
                    if(null == response){
                        httpResponse = new DefaultFullHttpResponse(fullRequest.protocolVersion(), HttpResponseStatus.BAD_REQUEST);
                    }else{
                        //文件
                        if (response.getFile() != null) {
                            RandomAccessFile raf = new RandomAccessFile(response.getFile(), "r");
                            long fileLength = raf.length();
                            //filename
                            httpResponse = new DefaultHttpResponse(request.getHttpVersion(), response.getStatus());
                            httpResponse.headers().add(response.headers());
                            httpResponse.headers().add(HttpHeaderName.TRANSFER_ENCODING, "chunked");
                            packageResponseHeader(httpResponse, fileLength, ctx);
                            packageFileHeaders(httpResponse, response.getFile());
                            ctx.write(httpResponse);
                            ctx.write(new HttpChunkedInput(new ChunkedFile(raf, 0, fileLength, serverConfig.getChunkSize())),ctx.newProgressivePromise());
                            sendResponse(ctx, LastHttpContent.EMPTY_LAST_CONTENT);
                            return;
                        }else if(response.getInputStream() != null){
                            httpResponse = new DefaultHttpResponse(request.getHttpVersion(), response.getStatus());
                            httpResponse.headers().add(response.headers());
                            httpResponse.headers().add(HttpHeaderName.TRANSFER_ENCODING, "chunked");
                            if(isKeepAlive(ctx.channel()) &&
                                    !response.headers().contains(HttpHeaderName.CONNECTION)){
                                response.headers().add(HttpHeaderName.CONNECTION, "keep-alive");
                            }
                            ctx.write(httpResponse);
                            ctx.write(new HttpChunkedInput(new ChunkedStream(response.getInputStream(),serverConfig.getChunkSize())),ctx.newProgressivePromise());
                            sendResponse(ctx, LastHttpContent.EMPTY_LAST_CONTENT);
                            return;
                        }else{
                            DefaultFullHttpResponse fullHttpResponse = new DefaultFullHttpResponse(response.getVersion(), response.getStatus());
                            fullHttpResponse.headers().clear();
                            fullHttpResponse.headers().add(response.headers());
                            if(response.content()!=null) {
                                fullHttpResponse.content().writeBytes(response.content());
                            }
                            httpResponse = fullHttpResponse;
                        }
                        sendResponse(ctx, httpResponse);
                        return;
                    }
                } catch (Exception e) {
                    logger.error("", e);
                    httpResponse = new DefaultFullHttpResponse(fullRequest.protocolVersion(), HttpResponseStatus.INTERNAL_SERVER_ERROR);
                }
                sendResponse(ctx, httpResponse);
            }
        });
    }

    private void packageFileHeaders(HttpResponse response, File fileToCache) {
        if(!response.headers().contains(HttpHeaderName.DATE)) {
            response.headers().set(HttpHeaderName.DATE, getDateFormatter().format(System.currentTimeMillis()));
        }
        if(!response.headers().contains(HttpHeaderName.CONTENT_TYPE)) {
            response.headers().set(HttpHeaderName.CONTENT_TYPE, MediaType.getContentType(fileToCache.getName()));
        }
        if(!response.headers().contains(HttpHeaderName.LAST_MODIFIED)) {
            response.headers().set(HttpHeaderName.LAST_MODIFIED, getDateFormatter().format(new Date(fileToCache.lastModified())));
        }
    }

    private Long getReqId(Channel channel) {
        Long reqId = channel.attr(Constants.ATTR_REQ_ID).get();
        //illegal request will occur NullPointException, so fix this exception
        if (null == reqId) {
            reqId = RequestIdGenerator.getRequestId();
            channel.attr(Constants.ATTR_REQ_ID).set(reqId);
        }
        return reqId;
    }

    protected boolean isKeepAlive(Channel channel) {
        if (!serverConfig.isOpenKeepAlive()) {
            return false;
        }
        Boolean keepAlive = channel.attr(Constants.ATTR_KEEP_ALIVE).get();
        return keepAlive==null?false:keepAlive;
    }

    protected HttpContextRequest prepareHttpRequest(ChannelHandlerContext ctx, FullHttpRequest fullRequest){
        long reqId = genReqId();
        ctx.channel().attr(Constants.ATTR_REQ_ID).set(reqId);
        int times = ctx.channel().attr(Constants.ATTR_HTTP_REQ_TIMES).get().decrementAndGet();
        boolean isKeepAlive = true;
        if(!HttpUtil.isKeepAlive(fullRequest) ||
                ( serverConfig.getMaxKeepAliveRequests() >= 0 && times <= 0)){
            isKeepAlive = false;
        }
        ctx.channel().attr(Constants.ATTR_KEEP_ALIVE).set(isKeepAlive);
        HttpContextRequest request = new HttpContextRequest(fullRequest, getServerConfig().getCharset());
        request.addAttachment(Constants.REQ_ID, reqId);
        return request;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ctx.channel().attr(Constants.ATTR_HTTP_REQ_TIMES).set(new AtomicInteger(serverConfig.getMaxKeepAliveRequests()));
        ctx.channel().attr(Constants.ATTR_CHANNEL_ACTIVE_TIME).set(System.currentTimeMillis());
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if(serverConfig.isOpenConnectCostLogger()) {
            Long reqId = getReqId(ctx.channel());
            long startTime = getActiveTime(ctx.channel());
            int times = serverConfig.getMaxKeepAliveRequests() - ctx.channel().attr(Constants.ATTR_HTTP_REQ_TIMES).get().get();
            logger.info("http finish, last reqId={}, keep-alive times={}, cost={}ms", reqId, times, System.currentTimeMillis() - startTime);
        }
        super.channelInactive(ctx);
    }

    private long getActiveTime(Channel channel) {
        return channel.attr(Constants.ATTR_CHANNEL_ACTIVE_TIME).get();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("reqId=" + getReqId(ctx.channel()) + ",active times=" + (System.currentTimeMillis() - getActiveTime(ctx.channel())) + "ms,address=" + ctx.channel().remoteAddress().toString(), cause);
        ctx.close();
//        FullHttpResponse response;
//        try {
//            response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST);
//            if (cause instanceof TooLongFrameException) {
//                response.setStatus(HttpResponseStatus.BAD_REQUEST);
//            }else {
//                response.setStatus(HttpResponseStatus.INTERNAL_SERVER_ERROR);
//            }
//            String ex = cause.getMessage();
//            if (null == ex) {
//                ex = String.valueOf(cause);
//            }
//            response.content().writeBytes(ex.getBytes(getServerConfig().getCharset()));
//        } catch (Exception e) {
//            logger.error("reqId="+getReqId(ctx.channel()), e);
//            response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST);
//        }
//        sendResponse(ctx, response);
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
        if(!response.headers().contains(HttpHeaderName.CONNECTION)) {
            response.headers().add(HttpHeaderName.CONNECTION,isKeepAlive(ctx.channel())?HttpHeaderValues.KEEP_ALIVE:HttpHeaderValues.CLOSE);
        }
    }
    private SimpleDateFormat getDateFormatter(){
        SimpleDateFormat dateFormatter = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);
        dateFormatter.setTimeZone(TimeZone.getTimeZone("GMT"));
        return dateFormatter;
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

    private Long genReqId(){
        return RequestIdGenerator.getRequestId();
    }
}
