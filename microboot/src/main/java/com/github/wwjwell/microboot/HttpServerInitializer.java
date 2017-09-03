package com.github.wwjwell.microboot;

import com.github.wwjwell.microboot.metrics.BytesMetricsHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpContentCompressor;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.ssl.ClientAuth;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLException;

/**
 * Created by wwj on 2017/6/21.
 */
public class HttpServerInitializer extends ChannelInitializer<SocketChannel> {
    private Logger logger = LoggerFactory.getLogger(HttpServerInitializer.class);
    private HttpContextHandler http1ServerHandler;
    private SslContext sslContext;
    private ServerConfig serverConfig;

    public HttpServerInitializer(ServerConfig serverConfig, HttpContextHandler http1ServerHandler) {
        this.serverConfig = serverConfig;
        this.http1ServerHandler = http1ServerHandler;
        if (serverConfig.isOpenSSL()) {
            if (null != serverConfig.getSslContext()) {
                sslContext = serverConfig.getSslContext();
            }else {
                Assert.isTrue(serverConfig.getKeyCertChainFilePath() != null, "cert file should't be null");
                Assert.isTrue(serverConfig.getKeyFilePath() != null, "private key file shouldn't be null");
                SslContextBuilder sslContextBuilder = MicroSslContext.forServer(serverConfig.getKeyCertChainFilePath(), serverConfig.getKeyFilePath(), serverConfig.getKeyPassword(), serverConfig.getTrustCertCollectionFilePath());
                sslContextBuilder.clientAuth(serverConfig.isSslClientAuth() ? ClientAuth.REQUIRE : ClientAuth.NONE);
                try {
                    sslContext = sslContextBuilder.build();
                } catch (SSLException e) {
                    logger.error("", e);
                    throw new RuntimeException(e.getMessage());
                }
            }
        }
    }
    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        if (null != sslContext) {
            SSLEngine sslEngine = sslContext.newEngine(ch.alloc());
            configureSsl(ch, new SslHandler(sslEngine));
        } else {
            configureClearText(ch);
        }
    }

    private void configureClearText(SocketChannel ch) {
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
        ch.pipeline().addLast("microhttp-codec", new MicrobootHttpCodec(serverConfig));
        ch.pipeline().addLast(http1ServerHandler);
    }

    /**
     * Configure the pipeline for TLS NPN negotiation to HTTP/2.
     */
    private void configureSsl(SocketChannel ch, SslHandler sslHandlere) {
        ch.pipeline().addLast(sslHandlere);
        ch.pipeline().addLast(new Http2OrHttp1Handler(serverConfig, http1ServerHandler));
    }
}
