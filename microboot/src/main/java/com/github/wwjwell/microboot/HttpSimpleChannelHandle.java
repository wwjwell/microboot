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
public class HttpSimpleChannelHandle extends SimpleChannelInboundHandler<HttpContextRequest> implements ApplicationContextAware,InitializingBean{
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
    protected void channelRead0(final ChannelHandlerContext ctx, final HttpContextRequest request)
            throws Exception {
        prepareHttpRequest(ctx, request);
        ctx.executor().execute(new Runnable() {
            @Override
            public void run() {
                //转化为 api 能处理的request\response
                HttpContextResponse response = new HttpContextResponse(request.getHttpVersion(), HttpResponseStatus.OK, getServerConfig().getCharset());
                try {
                    //compressor support
                    String accpetEncoding = request.getHeader(HttpHeaderName.ACCEPT_ENCODING);
                    if(null != accpetEncoding && accpetEncoding.length()>0) {
                        response.addHeader(HttpHeaderName.ACCEPT_ENCODING, accpetEncoding);
                    }
                    dispatcher.doService(request, response);
                } catch (Exception e) {
                    logger.error("", e);
                    response = new HttpContextResponse(request.getHttpVersion(), HttpResponseStatus.BAD_REQUEST, getServerConfig().getCharset());
                }
                sendResponse(ctx, response);
            }
        });
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

    protected HttpContextRequest prepareHttpRequest(ChannelHandlerContext ctx, HttpContextRequest request){
        long reqId = genReqId();
        ctx.channel().attr(Constants.ATTR_REQ_ID).set(reqId);
        int times = ctx.channel().attr(Constants.ATTR_HTTP_REQ_TIMES).get().decrementAndGet();
        boolean isKeepAlive = true;
        if(!isKeepAlive(request) ||
                ( serverConfig.getMaxKeepAliveRequests() >= 0 && times <= 0)){
            isKeepAlive = false;
        }
        ctx.channel().attr(Constants.ATTR_KEEP_ALIVE).set(isKeepAlive);
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

    protected void packageFullResponseHeader(HttpContextResponse response,ChannelHandlerContext ctx){
        long len = 0;
        if (response.content() != null) {
            len = response.content().readableBytes();
        }
        packageResponseHeader(response, ctx);

        if (!response.headers().contains(HttpHeaderName.CONTENT_LENGTH)) {
            response.headers().add(HttpHeaderName.CONTENT_LENGTH, len);
        }
    }

    protected void packageResponseHeader(HttpContextResponse response,ChannelHandlerContext ctx) {
        if(serverConfig.getHeaderServer() != null
                && serverConfig.getHeaderServer().length()>0 &&
                !response.headers().contains(HttpHeaderName.SERVER)){
            response.headers().add(HttpHeaderName.SERVER, serverConfig.getHeaderServer());
        }
        if(!response.headers().contains(HttpHeaderName.CONNECTION)) {
            response.headers().add(HttpHeaderName.CONNECTION,isKeepAlive(ctx.channel())?HttpHeaderValues.KEEP_ALIVE:HttpHeaderValues.CLOSE);
        }
    }


    protected void sendResponse(ChannelHandlerContext ctx, HttpContextResponse response) {
        packageFullResponseHeader(response, ctx);
        ctx.writeAndFlush(response).addListener(listener);
    }

    /**
     * 判断是否是keep-alive
     * @param request
     * @return
     */
    private boolean isKeepAlive(HttpContextRequest request) {
        CharSequence connection = request.getHeader(HttpHeaderNames.CONNECTION);
        if (connection != null && HttpHeaderValues.CLOSE.contentEqualsIgnoreCase(connection)) {
            return false;
        }

        if (request.getHttpVersion().isKeepAliveDefault()) {
            return !HttpHeaderValues.CLOSE.contentEqualsIgnoreCase(connection);
        } else {
            return HttpHeaderValues.KEEP_ALIVE.contentEqualsIgnoreCase(connection);
        }
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
