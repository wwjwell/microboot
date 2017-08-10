package com.github.wwjwell.microboot;

import com.github.wwjwell.microboot.metrics.BytesMetricsHandler;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http2.*;
import io.netty.handler.ssl.*;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by wwj on 2017/6/21.
 */
public class HttpServerInitializer extends ChannelInitializer<SocketChannel> {
    private Logger logger = LoggerFactory.getLogger(HttpServerInitializer.class);
    private Http1ServerHandler http1ServerHandler;
    private SslContext sslCtx;
    private ServerConfig serverConfig;

    public HttpServerInitializer(ServerConfig serverConfig, Http1ServerHandler http1ServerHandler) {
        this.serverConfig = serverConfig;
        this.http1ServerHandler = http1ServerHandler;
        if (!serverConfig.isOpenSSL()) {
            SslProvider provider = OpenSsl.isAlpnSupported() ? SslProvider.OPENSSL : SslProvider.JDK;
            try {
                SelfSignedCertificate ssc = new SelfSignedCertificate();
                sslCtx = SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey())
                        .sslProvider(provider)
                /* NOTE: the cipher filter may not include all ciphers required by the HTTP/2 specification.
                 * Please refer to the HTTP/2 specification for cipher requirements. */
                        .ciphers(Http2SecurityUtil.CIPHERS, SupportedCipherSuiteFilter.INSTANCE)
                        .applicationProtocolConfig(new ApplicationProtocolConfig(
                                ApplicationProtocolConfig.Protocol.ALPN,
                                // NO_ADVERTISE is currently the only mode supported by both OpenSsl and JDK providers.
                                ApplicationProtocolConfig.SelectorFailureBehavior.NO_ADVERTISE,
                                // ACCEPT is currently the only mode supported by both OpenSsl and JDK providers.
                                ApplicationProtocolConfig.SelectedListenerFailureBehavior.ACCEPT,
                                ApplicationProtocolNames.HTTP_2,
                                ApplicationProtocolNames.HTTP_1_1))
                        .build();
            } catch (Exception e) {
                logger.warn("", e);
            }
        } else {
            sslCtx = null;
        }
    }
    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        if (sslCtx != null) {
            configureSsl(ch);
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
    private void configureSsl(SocketChannel ch) {
        ch.pipeline().addLast(sslCtx.newHandler(ch.alloc()));
        ch.pipeline().addLast("microhttp-codec", new MicrobootHttpCodec(serverConfig));
        ch.pipeline().addLast(new Http2OrHttpHandler(serverConfig, http1ServerHandler));
    }
}
