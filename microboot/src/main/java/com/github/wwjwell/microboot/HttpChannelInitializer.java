package com.github.wwjwell.microboot;

import com.github.wwjwell.microboot.metrics.BytesMetricsHandler;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.codec.http.HttpContentCompressor;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.ssl.SslHandler;
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
        if (serverConfig.isOpenSSL()) {
            assert null != serverConfig.getSslEngine();
            ch.pipeline().addFirst("ssl", new SslHandler(serverConfig.getSslEngine()));
        }
        ch.pipeline().addLast("idle", new IdleStateHandler(0, 0, serverConfig.getKeepAliveTimeout()));
        if (serverConfig.isOpenMetricsLogger()) {
            ch.pipeline().addFirst("metrics", new BytesMetricsHandler());
        }
        ch.pipeline().addLast("codec", new HttpServerCodec());
        if(serverConfig.isOpenCompression()) {
            ch.pipeline().addLast("compressor", new HttpContentCompressor(serverConfig.getCompressionLevel())); //启用压缩
        }
        ch.pipeline().addLast("aggregator", new HttpObjectAggregator(serverConfig.getMaxLength()));
        ch.pipeline().addLast("chunk", new ChunkedWriteHandler());
        ch.pipeline().addLast(handle);
    }
}
