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
public class Http2OrHttp1Handler extends ApplicationProtocolNegotiationHandler {
    private ServerConfig serverConfig;
    private HttpContextHandler httpContextHandler;

    protected Http2OrHttp1Handler(ServerConfig serverConfig, HttpContextHandler httpContextHandler) {
        super(ApplicationProtocolNames.HTTP_1_1);
        this.serverConfig = serverConfig;
        this.httpContextHandler = httpContextHandler;
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
            ctx.pipeline().addLast(httpContextHandler);
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
            ctx.pipeline().addLast(httpContextHandler);
            return;
        }

        throw new IllegalStateException("unknown protocol: " + protocol);
    }
}