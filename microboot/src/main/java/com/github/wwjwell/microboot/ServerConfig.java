package com.github.wwjwell.microboot;

import com.github.wwjwell.microboot.constants.Constants;
import io.netty.channel.epoll.Epoll;

import javax.net.ssl.SSLEngine;
import java.nio.charset.Charset;
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

    //about tcp setting
    private int SO_SNDBUF = 2 * 1024;
    private int SO_RCVBUF = 10 * 1024;
    private int SO_BACKLOG = 1024;
    private boolean SO_REUSEADDR = false;
    private int CONNECT_TIMEOUT_MILLIS = 3000;     //连接超时时间

    //end

    //keep alive
    private boolean openKeepAlive = true;   //openKeepAlive
    private int keepAliveTimeout = 30000;   //在keepAliveTimeout长时间内没有通信，会关闭掉该连接。设置为-1 则代表不会关闭该连接。
    private int maxKeepAliveRequests = 100; //默认为100，也就是在keepAliveTimeout时间内，如果使用次数超过100，则会关闭掉该连接。设置为-1，则代表不会关闭连接。在关闭后，会在返回的header上面加上Connection:close 。
    private int maxLength = 1024 * 1024;      //http报文最大长度 ,default  1M
    private int chunkSize = 8192; //HTTP chunk size
    private boolean openSSL = false;    //启用SSL
    private SSLEngine sslEngine = null; //ssl
    /**
     * compressionLevel (0-9),1 yields the fastest compression and 9 yields the
     * best compression.  0 means no compression.  The default
     * compression level is 6
     */
    private boolean openCompression = false;    //启用压缩
    private int compressionLevel = 6;
    private int bossThreadNum;          //netty boss Thread
    private int workerThreadNum;        //netty work thread
    private boolean openMetricsLogger = false; //流量统计
    private boolean openConnectCostLogger = false; //连接耗时日志
    private String headerServer = Constants.SERVER;
    private Executor executor;

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

    public int getChunkSize() {
        return chunkSize;
    }

    public void setChunkSize(int chunkSize) {
        this.chunkSize = chunkSize;
    }

    public void setWorkerThreadNum(int workerThreadNum) {
        this.workerThreadNum = workerThreadNum;
    }

    public boolean isOpenMetricsLogger() {
        return openMetricsLogger;
    }

    public void setOpenMetricsLogger(boolean openMetricsLogger) {
        this.openMetricsLogger = openMetricsLogger;
    }

    public boolean isOpenConnectCostLogger() {
        return openConnectCostLogger;
    }

    public void setOpenConnectCostLogger(boolean openConnectCostLogger) {
        this.openConnectCostLogger = openConnectCostLogger;
    }

    public int getKeepAliveTimeout() {
        return keepAliveTimeout;
    }

    public void setKeepAliveTimeout(int keepAliveTimeout) {
        this.keepAliveTimeout = keepAliveTimeout;
    }

    public int getMaxKeepAliveRequests() {
        return maxKeepAliveRequests;
    }

    public void setMaxKeepAliveRequests(int maxKeepAliveRequests) {
        this.maxKeepAliveRequests = maxKeepAliveRequests;
    }

    public int getCONNECT_TIMEOUT_MILLIS() {
        return CONNECT_TIMEOUT_MILLIS;
    }

    public void setCONNECT_TIMEOUT_MILLIS(int CONNECT_TIMEOUT_MILLIS) {
        this.CONNECT_TIMEOUT_MILLIS = CONNECT_TIMEOUT_MILLIS;
    }

    public int getSO_SNDBUF() {
        return SO_SNDBUF;
    }

    public void setSO_SNDBUF(int SO_SNDBUF) {
        this.SO_SNDBUF = SO_SNDBUF;
    }

    public int getSO_RCVBUF() {
        return SO_RCVBUF;
    }

    public void setSO_RCVBUF(int SO_RCVBUF) {
        this.SO_RCVBUF = SO_RCVBUF;
    }

    public int getSO_BACKLOG() {
        return SO_BACKLOG;
    }

    public boolean isSO_REUSEADDR() {
        return SO_REUSEADDR;
    }

    public void setSO_REUSEADDR(boolean SO_REUSEADDR) {
        this.SO_REUSEADDR = SO_REUSEADDR;
    }

    public void setSO_BACKLOG(int SO_BACKLOG) {
        this.SO_BACKLOG = SO_BACKLOG;
    }

    public String getHeaderServer() {
        return headerServer;
    }

    public void setHeaderServer(String headerServer) {
        this.headerServer = headerServer;
    }

    public Executor getExecutor() {
        return executor;
    }

    public void setExecutor(Executor executor) {
        this.executor = executor;
    }

    public boolean isOpenSSL() {
        return openSSL;
    }

    public void setOpenSSL(boolean openSSL) {
        this.openSSL = openSSL;
    }

    public SSLEngine getSslEngine() {
        return sslEngine;
    }

    public void setSslEngine(SSLEngine sslEngine) {
        this.sslEngine = sslEngine;
    }

    public boolean isOpenCompression() {
        return openCompression;
    }

    public void setOpenCompression(boolean openCompression) {
        this.openCompression = openCompression;
    }

    public int getCompressionLevel() {
        return compressionLevel;
    }

    public void setCompressionLevel(int compressionLevel) {
        this.compressionLevel = compressionLevel;
    }

    public boolean isOpenKeepAlive() {
        return openKeepAlive;
    }

    public void setOpenKeepAlive(boolean openKeepAlive) {
        this.openKeepAlive = openKeepAlive;
    }
}
