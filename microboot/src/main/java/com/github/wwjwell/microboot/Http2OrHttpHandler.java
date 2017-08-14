package com.github.wwjwell.microboot;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpContentCompressor;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http2.*;
import io.netty.handler.ssl.ApplicationProtocolNames;
import io.netty.handler.ssl.ApplicationProtocolNegotiationHandler;
import io.netty.handler.stream.ChunkedWriteHandler;

/**
 * Created by wwj on 2017/8/3.
 */
public class Http2OrHttpHandler extends ApplicationProtocolNegotiationHandler {
    private ServerConfig serverConfig;
    private Http1ServerHandler http1ServerHandler;

    protected Http2OrHttpHandler(ServerConfig serverConfig,Http1ServerHandler http1ServerHandler) {
        super(ApplicationProtocolNames.HTTP_1_1);
        this.serverConfig = serverConfig;
        this.http1ServerHandler = http1ServerHandler;
    }

    @Override
    protected void configurePipeline(ChannelHandlerContext ctx, String protocol) throws Exception {
        if (ApplicationProtocolNames.HTTP_2.equals(protocol)) {
            DefaultHttp2Connection connection = new DefaultHttp2Connection(true);
            InboundHttp2ToHttpAdapter listener = new InboundHttp2ToHttpAdapterBuilder(connection)
                    .propagateSettings(true).validateHttpHeaders(false)
                    .maxContentLength(serverConfig.getMaxLength()).build();
            ctx.pipeline().addLast(new HttpToHttp2ConnectionHandlerBuilder()
                    .frameListener(listener)
                    // .frameLogger(TilesHttp2ToHttpHandler.logger)
                    .connection(connection).build());
            ctx.pipeline().addLast("microhttp-codec", new MicrobootHttpCodec(serverConfig));
            ctx.pipeline().addLast(http1ServerHandler);
            return;
        }
        if (ApplicationProtocolNames.HTTP_1_1.equals(protocol)) {
            ctx.pipeline().addLast("codec", new HttpServerCodec());
            if(serverConfig.isOpenCompression()) {
                ctx.pipeline().addLast("compressor", new HttpContentCompressor(serverConfig.getCompressionLevel())); //启用压缩
            }
            ctx.pipeline().addLast("aggregator", new HttpObjectAggregator(serverConfig.getMaxLength()));
            ctx.pipeline().addLast("chunk", new ChunkedWriteHandler());
            ctx.pipeline().addLast("microhttp-codec", new MicrobootHttpCodec(serverConfig));
            ctx.pipeline().addLast(http1ServerHandler);
            return;
        }

        throw new IllegalStateException("unknown protocol: " + protocol);
    }
}