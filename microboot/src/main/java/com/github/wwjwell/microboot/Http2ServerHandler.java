package com.github.wwjwell.microboot;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http2.*;
import io.netty.util.CharsetUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Map;

import static io.netty.buffer.Unpooled.copiedBuffer;
import static io.netty.buffer.Unpooled.unreleasableBuffer;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;

/**
 * Created by wwj on 2017/8/3.
 */
public class Http2ServerHandler extends ChannelDuplexHandler{
    static final ByteBuf RESPONSE_BYTES = unreleasableBuffer(copiedBuffer("Hello World", CharsetUtil.UTF_8));

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof Http2HeadersFrame) {
            onHeadersRead(ctx, (Http2HeadersFrame) msg);
        } else if (msg instanceof Http2DataFrame) {
            Http2DataFrame dataFrame = (Http2DataFrame) msg;
            dataFrame.content().retain();
            System.out.println("data:");
            System.out.println("\t" + dataFrame.content().toString(CharsetUtil.UTF_8));
            dataFrame.content().release();
            onDataRead(ctx, (Http2DataFrame) msg);
        } else {
            super.channelRead(ctx, msg);
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    /**
     * If receive a frame with end-of-stream set, send a pre-canned response.
     */
    private static void onDataRead(ChannelHandlerContext ctx, Http2DataFrame data) throws Exception {
        if (data.isEndStream()) {
            sendResponse(ctx, data.content());
        } else {
            // We do not send back the response to the remote-peer, so we need to release it.
            data.release();
        }
    }

    /**
     * If receive a frame with end-of-stream set, send a pre-canned response.
     */
    private static void onHeadersRead(ChannelHandlerContext ctx, Http2HeadersFrame headers)
            throws Exception {
        System.out.println("stream:" + headers.streamId());
        for (Map.Entry<CharSequence, CharSequence> entry : headers.headers()) {
            System.out.println("\t" + entry.getKey() + ":" + entry.getValue());
        }
        String path = headers.headers().path().toString();
        Http2Headers header = new DefaultHttp2Headers().status(OK.codeAsText());
        if(headers.isEndStream()) {
            if (path.endsWith(".js")) {
                ByteBuf content = ctx.alloc().buffer();
                header.set(HttpHeaderNames.CONTENT_TYPE, "text/javascript; charset=UTF-8");
                ByteBufUtil.writeAscii(content, "console.log('hello world');");
                ctx.writeAndFlush(new DefaultHttp2HeadersFrame(header, false));
                ctx.write(new DefaultHttp2DataFrame(content, true));
            } else if (path.endsWith(".png")) {
                File file = new File("/Users/wwj/Desktop/htt2.png");
                InputStream in = new FileInputStream(file);
                ByteBuf content = ctx.alloc().buffer();
                header.set(HttpHeaderNames.CONTENT_TYPE, "image/png");
                byte[] buf = new byte[1024];
                int len = in.read(buf);
                while (len>=1024) {
                    content.writeBytes(buf);
                    len = in.read(buf);
                }
                if (len > 0) {
                    content.writeBytes(buf, 0, len);
                }
                ctx.writeAndFlush(new DefaultHttp2HeadersFrame(header, false));
                ctx.write(new DefaultHttp2DataFrame(content, true));
            } else {
                ctx.writeAndFlush(new DefaultHttp2HeadersFrame(header, false));
                ByteBuf content = ctx.alloc().buffer();
                String html = "<!doctype html><html>";
                html += "<header>";
                html += "<script src='/test.js'></script>";
                html += "</header>";
                html += "<body>";
                html += "<h1>THIS is BODY</ht>";
                html += "<form method='post' action='/t'>" +
                        "<input type='text' name='name' value='wwj'/>" +
                        "<input type='submit' value='submit'/>" +
                        "</form>";
                html += "<img src='test.png'/>";
                html += "</body>";

                html+="</html>";

//            content.writeBytes(RESPONSE_BYTES.duplicate());
//            ByteBufUtil.writeAscii(content, " - via HTTP/2");
                ByteBufUtil.writeAscii(content, html);
                ctx.write(new DefaultHttp2DataFrame(content, true));

            }
        }
    }
    /**
     * Sends a "Hello World" DATA frame to the client.
     */
    private static void sendResponse(ChannelHandlerContext ctx, ByteBuf payload) {
        // Send a frame for the response status
        Http2Headers header = new DefaultHttp2Headers().status(OK.codeAsText());
        ctx.writeAndFlush(new DefaultHttp2HeadersFrame(header, false));

        ByteBuf content = ctx.alloc().buffer();
        String html = "<!doctype html><html>";
        html += "<header>";
        html += "<script src='/test.js'></script>";
        html += "</header>";
        html += "<body>";
        html += "<h1>THIS is BODY</ht>";
        html += "<form method='post' action='/t'>" +
                "<input type='text' name='name' value='wwj'/>" +
                "<input type='submit' value='submit'/>" +
                "</form>";
        html += "<img src='test.png'/>";
        html += "<br/><div>" + payload.toString(CharsetUtil.UTF_8)+"</div>";
        html += "</body>";

        html+="</html>";

//            content.writeBytes(RESPONSE_BYTES.duplicate());
//            ByteBufUtil.writeAscii(content, " - via HTTP/2");
        ByteBufUtil.writeAscii(content, html);
        ctx.write(new DefaultHttp2DataFrame(content, true));
    }
}
