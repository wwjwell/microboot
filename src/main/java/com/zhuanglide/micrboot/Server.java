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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.nio.charset.Charset;

/**
 * http server
 * Created by wwj on 17/3/2.
 */
public class Server implements ApplicationContextAware,InitializingBean {
    private Logger logger = LoggerFactory.getLogger(Server.class);
    private String charset = "UTF-8"; //默认编码
    private boolean useEpoll = false;
    private int port;
    //系统处理线程数，默认为当前CPU的核心数的2倍
    private int threadNum = Runtime.getRuntime().availableProcessors() * 2;
    private int bossThreads = threadNum ; //netty boss 线程
    private int workThreads =  2 * bossThreads; //netty work线程
    private int maxLength = 65536; //http报文最大长度
    private boolean useChunked = false;
    private ApplicationContext context;
    private HttpSimpleChannelHandle httpSimpleChannelHandle;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    /**
     * Http服务启动
     * 系统异步线程方式启动起来
     */
    public void start(){
        Class<? extends ServerChannel> socketChannelClass;
        if(epollAvailable()) {
            bossGroup = new EpollEventLoopGroup(bossThreads);
            workerGroup = new EpollEventLoopGroup(workThreads);
            socketChannelClass = EpollServerSocketChannel.class;
        }else{
            bossGroup = new NioEventLoopGroup(bossThreads);
            workerGroup = new NioEventLoopGroup(workThreads);
            socketChannelClass = NioServerSocketChannel.class;
        }
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup).channel(socketChannelClass)
                    .childHandler(new ChannelInitializer<SocketChannel>() { // (4)
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
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
        } catch (InterruptedException e) {
            logger.error("", e);
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

    public void start(int port){
        setPort(port);
        start();
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

    public void setBossThreads(int bossThreads) {
        this.bossThreads = bossThreads;
    }

    public void setWorkThreads(int workThreads) {
        this.workThreads = workThreads;
    }

    public void setUseEpoll(boolean useEpoll) {
        this.useEpoll = useEpoll;
    }

    public int getThreadNum() {
        return threadNum;
    }

    public int getBossThreads() {
        return bossThreads;
    }

    public int getWorkThreads() {
        return workThreads;
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
