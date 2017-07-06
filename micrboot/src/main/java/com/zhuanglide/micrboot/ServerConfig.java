package com.zhuanglide.micrboot;

import com.zhuanglide.micrboot.constants.Constants;
import io.netty.channel.epoll.Epoll;
import org.slf4j.Logger;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.concurrent.Executor;

/**
 * Created by wwj on 2017/5/18.
 */
public class ServerConfig {
    private static final int DEFAULT_BASE_THREAD_NUM;
    static {
        DEFAULT_BASE_THREAD_NUM = Math.max(1, Runtime.getRuntime().availableProcessors());
    }

    public ServerConfig(){
    }
    private static ServerConfig defaultServerConfig;

    public static ServerConfig defaultServerConfig(){
        if (null == defaultServerConfig) {
            synchronized (ServerConfig.class) {
                if(null == defaultServerConfig) {
                    defaultServerConfig = new ServerConfig();
                }
            }
        }
        return defaultServerConfig;
    }


    private Charset charset = Charset.forName("UTF-8");   //默认编码
    private boolean useEpoll = true;    //使用epoll
    private int port = 8080;            //端口
    private int idleTimeout = 30;              //超时时间
    private int maxLength = 1024*1024;      //http报文最大长度
    private int chunkSize = 8192; //HTTP chunk size
    private boolean openMetrics = false; //加入性能监控
    private boolean openConnectCostLogger = false; //连接耗时日志
    private int bossThreadNum;          //netty boss Thread
    private int workerThreadNum;        //netty work thread
    private Executor executor;
    private String headerServer = Constants.SERVER; //Server:micrboot ?

    public boolean epollAvailable(){
        return useEpoll && Epoll.isAvailable();
    }

    public int getBossThreadNum() {
        if (bossThreadNum < 1) {
            bossThreadNum = 1;
        }
        return bossThreadNum;
    }

    public void setBossThreadNum(int bossThreadNum) {
        this.bossThreadNum = bossThreadNum;
    }

    public int getWorkerThreadNum() {
        if (workerThreadNum < 1) {
            workerThreadNum = Math.max(1, DEFAULT_BASE_THREAD_NUM * 2);
        }
        return workerThreadNum;
    }

    public int getChunkSize() {
        return chunkSize;
    }

    public void setChunkSize(int chunkSize) {
        this.chunkSize = chunkSize;
    }

    public Charset getCharset() {
        return charset;
    }

    public void setCharset(Charset charset) {
        this.charset = charset;
    }

    public boolean isUseEpoll() {
        return useEpoll;
    }

    public void setUseEpoll(boolean useEpoll) {
        this.useEpoll = useEpoll;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getMaxLength() {
        return maxLength;
    }

    public void setMaxLength(int maxLength) {
        this.maxLength = maxLength;
    }

    public void setWorkerThreadNum(int workerThreadNum) {
        this.workerThreadNum = workerThreadNum;
    }

    public boolean isOpenMetrics() {
        return openMetrics;
    }

    public void setOpenMetrics(boolean openMetrics) {
        this.openMetrics = openMetrics;
    }

    public boolean isOpenConnectCostLogger() {
        return openConnectCostLogger;
    }

    public void setOpenConnectCostLogger(boolean openConnectCostLogger) {
        this.openConnectCostLogger = openConnectCostLogger;
    }

    public Executor getExecutor() {
        return executor;
    }

    public void setExecutor(Executor executor) {
        this.executor = executor;
    }

    public int getIdleTimeout() {
        return idleTimeout;
    }

    public void setIdleTimeout(int idleTimeout) {
        this.idleTimeout = idleTimeout;
    }

    public String getHeaderServer() {
        return headerServer;
    }

    public void setHeaderServer(String headerServer) {
        this.headerServer = headerServer;
    }
}
