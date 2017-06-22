package com.zhuanglide.micrboot;

import com.zhuanglide.micrboot.metrics.BytesMetricsHandler;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;

/**
 * Created by wwj on 2017/6/21.
 */
public class HttpChannelInitializer extends ChannelInitializer {
    private HttpSimpleChannelHandle handle;
    private ServerConfig serverConfig;
    public HttpChannelInitializer(ServerConfig serverConfig, HttpSimpleChannelHandle handle) {
        this.serverConfig = serverConfig;
        this.handle = handle;
    }
    @Override
    protected void initChannel(Channel ch) throws Exception {
        ch.pipeline().addLast("idle",new IdleStateHandler(0,0,serverConfig.getIdleTimeout()));
        ch.pipeline().addLast("codec", new HttpServerCodec());
        ch.pipeline().addLast("aggregator",new HttpObjectAggregator(serverConfig.getMaxLength()));
        if(serverConfig.isUseChunked()) {//是否起用文件的大数据流
            ch.pipeline().addLast("chunk", new ChunkedWriteHandler());
        }
        //流量监控
        if (serverConfig.isOpenMetrics()) {
            ch.pipeline().addLast("metrics", new BytesMetricsHandler());
        }
        ch.pipeline().addLast(handle);
    }
}
