package com.zhuanglide.micrboot;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.ServerChannel;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.nio.charset.Charset;
import java.util.concurrent.Executor;

/**
 * http server
 * Created by wwj on 17/3/2.
 */
public class Server implements ApplicationContextAware,InitializingBean {
    private Logger logger = LoggerFactory.getLogger(Server.class);
    private String charset = "UTF-8"; //默认编码
    private boolean useEpoll = true;
    private int port;

    private static final int DEFAULT_EVENT_LOOP_THREADS;
    static {
        DEFAULT_EVENT_LOOP_THREADS = Math.max(1, Runtime.getRuntime().availableProcessors() * 2);
    }
    private int threadNum;
    private int maxLength = 65536; //http报文最大长度
    private boolean useChunked = false;
    private int bossThreadNum;
    private int workerThreadNum;
    private Executor executor;
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
                    .childHandler(new ChannelInitializer<SocketChannel>() { // (4)
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new IdleStateHandler(30, 30, 30));
                            ch.pipeline().addLast(new HttpServerCodec());
                            ch.pipeline().addLast(new HttpObjectAggregator(maxLength));
                            if(isUseChunked()) {//是否起用文件的大数据流
                                ch.pipeline().addLast(new ChunkedWriteHandler());
                            }
                            ch.pipeline().addLast(httpSimpleChannelHandle);
                        }
                    }).option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.TCP_NODELAY,true)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);
            logger.info("server start at port {}",port);
            ChannelFuture f = b.bind(port).sync();
            f.channel().closeFuture().sync();
        } catch (Exception e) {
            logger.error("", e);
        } finally {
            shutdown();
        }
    }

    public void start(int port){
        setPort(port);
        start();
    }


    /**
     * 优雅的关闭
     */
    public void shutdown(){
        try {
            if (null != bossGroup) {
                bossGroup.shutdownGracefully();
            }
            if (null != workerGroup) {
                workerGroup.shutdownGracefully();
            }
        } catch (Exception e) {
            logger.error("", e);
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
            httpSimpleChannelHandle.setCharset(Charset.forName(charset));
        }
    }

    /**
     * 初始化eventGroup 初始化channel
     */
    private void intEventGroupAndChannel(){
        if(epollAvailable()) {
            if(null == getExecutor()) {
                bossGroup = new EpollEventLoopGroup(getBossThreadNum());
                workerGroup = new EpollEventLoopGroup(getWorkerThreadNum());
            }else{
                bossGroup = new EpollEventLoopGroup(getBossThreadNum(), getExecutor());
                workerGroup = new EpollEventLoopGroup(getWorkerThreadNum(), getExecutor());
            }
            socketChannelClass = EpollServerSocketChannel.class;
        }else{
            if(null == getExecutor()) {
                bossGroup = new NioEventLoopGroup(getBossThreadNum());
                workerGroup = new NioEventLoopGroup(getWorkerThreadNum());
            }else{
                bossGroup = new NioEventLoopGroup(getBossThreadNum(), getExecutor());
                workerGroup = new NioEventLoopGroup(getWorkerThreadNum(), getExecutor());
            }
            socketChannelClass = NioServerSocketChannel.class;
        }
    }



    public String getCharset() {
        return charset;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    public int getMaxLength() {
        return maxLength;
    }

    public void setMaxLength(int maxLength) {
        this.maxLength = maxLength;
    }

    public void setThreadNum(int threadNum) {
        this.threadNum = threadNum;
    }

    public void setUseEpoll(boolean useEpoll) {
        this.useEpoll = useEpoll;
    }

    public int getThreadNum() {
        if (threadNum < 1) {
            threadNum = DEFAULT_EVENT_LOOP_THREADS;
        }
        return threadNum;
    }


    public boolean isUseChunked() {
        return useChunked;
    }

    public void setUseChunked(boolean useChunked) {
        this.useChunked = useChunked;
    }

    private boolean epollAvailable(){
        return useEpoll && Epoll.isAvailable();
    }

    public Executor getExecutor() {
        return executor;
    }

    public void setExecutor(Executor executor) {
        this.executor = executor;
    }

    public int getBossThreadNum() {
        if (bossThreadNum < 1) {
            bossThreadNum = getThreadNum();
        }
        return bossThreadNum;
    }

    public void setBossThreadNum(int bossThreadNum) {
        this.bossThreadNum = bossThreadNum;
    }

    public int getWorkerThreadNum() {
        if (workerThreadNum < 1) {
            workerThreadNum = getBossThreadNum() * 2;
        }
        return workerThreadNum;
    }

    public void setWorkerThreadNum(int workerThreadNum) {
        this.workerThreadNum = workerThreadNum;
    }


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
    }

    public ApplicationContext getContext() {
        return context;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        initStrategies(getContext());
    }
}
