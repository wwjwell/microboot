package com.github.wwjwell.microboot.metrics;

import com.github.wwjwell.microboot.constants.Constants;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BytesMetricsHandler extends ChannelDuplexHandler {
    private static final Logger logger = LoggerFactory.getLogger(BytesMetricsHandler.class);
    protected static final AttributeKey<BytesMetrics> ATTR_KEY_METRICS = AttributeKey.valueOf("BytesMetrics");

    public BytesMetricsHandler() {
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Attribute<BytesMetrics> attr = ctx.channel().attr(ATTR_KEY_METRICS);
        attr.set(new BytesMetrics());
        super.channelActive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        BytesMetrics metrics = ctx.channel().attr(ATTR_KEY_METRICS).get();
        metrics.incrementRead(((ByteBuf)msg).readableBytes());
        ctx.fireChannelRead(msg);
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        BytesMetrics metrics = ctx.channel().attr(ATTR_KEY_METRICS).get();
        metrics.incrementWrote(((ByteBuf)msg).writableBytes());
        ctx.write(msg, promise);
    }

    @Override
    public void close(ChannelHandlerContext ctx,
                      ChannelPromise promise) throws Exception {
        long reqId = ctx.channel().attr(Constants.ATTR_REQ_ID).get();
        BytesMetrics bytesMetrics = ctx.channel().attr(ATTR_KEY_METRICS).get();
        if(logger.isDebugEnabled()) {
            logger.debug("metrics reqId={}, readBytes={}, wroteBytes={}", reqId, bytesMetrics.readBytes(), bytesMetrics.wroteBytes());
        }
        super.close(ctx, promise);
    }
}
