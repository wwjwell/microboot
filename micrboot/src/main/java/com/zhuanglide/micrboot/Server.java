package com.zhuanglide.micrboot;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.stream.ChunkedWriteHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * http server
 * Created by wwj on 17/3/2.
 */
public class Server implements ApplicationContextAware,InitializingBean,DisposableBean {
    private Logger logger = LoggerFactory.getLogger(Server.class);
    private ServerConfig serverConfig;
    private ApplicationContext context;
    private HttpSimpleChannelHandle httpSimpleChannelHandle;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private Class<? extends ServerChannel> socketChannelClass;
    /**
     * Http服务启动
     * 系统异步线程方式启动起来
     */
    public void start(){
        intEventGroupAndChannel();

        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup).channel(socketChannelClass)
                    .childHandler(new HttpChannelInitializer(serverConfig,httpSimpleChannelHandle))
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.TCP_NODELAY,true)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);
            logger.info("server start at port {}",getServerConfig().getPort());
            ChannelFuture f = b.bind(getServerConfig().getPort()).sync();
            f.channel().closeFuture().sync();
        } catch (Exception e) {
            logger.error("", e);
        } finally {
            shutdown();
        }
    }


    /**
     * 优雅的关闭
     */
    private volatile boolean shutdown = false;
    public synchronized void shutdown(){
        if(!shutdown) {
            long time = System.currentTimeMillis();
            logger.info("server shutdownGracefully ...");
            try {

                if (null != bossGroup && !bossGroup.isShutdown()) {
                    bossGroup.shutdownGracefully();
                }
                if (null != workerGroup && !workerGroup.isShutdown()) {
                    workerGroup.shutdownGracefully();
                }
            } catch (Exception e) {
                logger.error("", e);
            }
            logger.info("server shutdown finish ,cost=" + (System.currentTimeMillis() - time) + "ms");
            shutdown = true;
        }
    }


    /**
     * 初始化
     * @param context
     */
    protected void initStrategies(ApplicationContext context) {
        try {
            httpSimpleChannelHandle = context.getBean(HttpSimpleChannelHandle.class);
        } catch (NoSuchBeanDefinitionException e) {
            httpSimpleChannelHandle = context.getAutowireCapableBeanFactory().createBean(HttpSimpleChannelHandle.class);
            httpSimpleChannelHandle.setServerConfig(getServerConfig());
        }
    }

    /**
     * 初始化eventGroup 初始化channel
     */
    private void intEventGroupAndChannel(){
        if(getServerConfig().epollAvailable()) {
            bossGroup = new EpollEventLoopGroup(getServerConfig().getBossThreadNum());
            workerGroup = new EpollEventLoopGroup(getServerConfig().getWorkerThreadNum(), getServerConfig().getExecutor());
            socketChannelClass = EpollServerSocketChannel.class;
        }else{
            bossGroup = new NioEventLoopGroup(getServerConfig().getBossThreadNum());
            workerGroup = new NioEventLoopGroup(getServerConfig().getWorkerThreadNum(), getServerConfig().getExecutor());
            socketChannelClass = NioServerSocketChannel.class;
        }
    }


    public void setServerConfig(ServerConfig serverConfig) {
        this.serverConfig = serverConfig;
    }

    public ServerConfig getServerConfig() {
        if (null == serverConfig) {
            serverConfig = ServerConfig.defaultServerConfig();
        }
        return serverConfig;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
    }

    @Override
    public void destroy() throws Exception {
        shutdown();
    }

    public ApplicationContext getContext() {
        return context;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        initStrategies(getContext());
    }
}
