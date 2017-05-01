package com.zhuanglide.micrboot;

import com.zhuanglide.micrboot.http.HttpContextRequest;
import com.zhuanglide.micrboot.http.HttpContextResponse;
import com.zhuanglide.micrboot.mvc.ApiDispatcher;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.TooLongFrameException;
import io.netty.handler.codec.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.nio.charset.Charset;

/**
 * HTTP handle
 */
@Sharable
public class HttpSimpleChannelHandle extends SimpleChannelInboundHandler<FullHttpRequest> implements ApplicationContextAware,InitializingBean{
    private Logger logger = LoggerFactory.getLogger(HttpSimpleChannelHandle.class);
    private Charset charset; //系统编码
    private ApplicationContext context;
    private ApiDispatcher dispatcher;
    /**
     * 初始化
     * @param context
     */
    protected void initStrategies(ApplicationContext context) {
        initApiMethodDispatcher(context);
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
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest fullRequest)
            throws Exception {
        if (fullRequest.decoderResult().isFailure()) {
            ctx.writeAndFlush(new DefaultHttpResponse(fullRequest.protocolVersion(), HttpResponseStatus.BAD_REQUEST)).addListener(ChannelFutureListener.CLOSE);
            return;
        }
        //转化为 api 能处理的request\response
        HttpContextRequest request = new HttpContextRequest(fullRequest, ctx.channel());
        HttpContextResponse response = new HttpContextResponse(fullRequest.protocolVersion(), HttpResponseStatus.OK, charset);
        dispatcher.doService(request, response);
        FullHttpResponse fullHttpResponse;
        if(null == response){
            fullHttpResponse = new DefaultFullHttpResponse(fullRequest.protocolVersion(), HttpResponseStatus.BAD_REQUEST);
        }else{
            fullHttpResponse = response.getHttpResponse();
        }
        sendResponse(fullHttpResponse, ctx);
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("", cause);
        try {
            FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST);
            if (cause instanceof TooLongFrameException) {
                response.setStatus(HttpResponseStatus.BAD_REQUEST);
            }else {
                response.setStatus(HttpResponseStatus.INTERNAL_SERVER_ERROR);
            }
            String ex = cause.getMessage();
            if (null == ex) {
                ex = String.valueOf(cause);
            }
            response.content().writeBytes(ex.getBytes(charset));
            sendResponse(response, ctx);
        } catch (Exception e) {
            logger.error("", e);
            ctx.close();
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        super.userEventTriggered(ctx, evt);
        ctx.close();
    }

    private void sendResponse(FullHttpResponse response, ChannelHandlerContext ctx) {
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    public void setCharset(Charset charset) {
        this.charset = charset;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        initStrategies(context);
    }


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
    }

}
