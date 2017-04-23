package com.zhuanglide.micrboot;

import com.zhuanglide.micrboot.http.HttpContextRequest;
import com.zhuanglide.micrboot.http.HttpContextResponse;
import com.zhuanglide.micrboot.mvc.ApiDispatcher;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.Assert;

import java.nio.charset.Charset;

/**
 * HTTP handle
 */
@Sharable
public class HttpSimpleChannelHandle extends SimpleChannelInboundHandler<FullHttpRequest> implements ApplicationContextAware,InitializingBean{
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
        Assert.isTrue(dispatcher != null);
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
        HttpContextResponse response = new HttpContextResponse(fullRequest.protocolVersion(),
                                                                                                              HttpResponseStatus.OK,charset);

        dispatcher.doService(request, response);

        FullHttpResponse fullHttpResponse;
        if(null == response){
            fullHttpResponse = new DefaultFullHttpResponse(fullRequest.protocolVersion(), HttpResponseStatus.BAD_REQUEST);
        }else{
            fullHttpResponse = response.getHttpResponse();
        }

        ctx.writeAndFlush(fullHttpResponse).addListener(ChannelFutureListener.CLOSE);
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        super.userEventTriggered(ctx, evt);
        ctx.close();
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

    private boolean isKeepAlive(HttpRequest request){
        return HttpUtil.isKeepAlive(request);
    }

}
