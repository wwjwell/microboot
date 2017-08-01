package com.github.wwjwell.microboot;

import com.github.wwjwell.microboot.http.HttpContextRequest;
import com.github.wwjwell.microboot.http.HttpContextResponse;
import com.github.wwjwell.microboot.http.HttpHeaderName;
import com.github.wwjwell.microboot.http.MediaType;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import io.netty.handler.codec.http.*;
import io.netty.handler.stream.ChunkedFile;
import io.netty.handler.stream.ChunkedStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by wwj on 2017/8/1.
 */
public class MicrobootHttpCodec extends MessageToMessageCodec<FullHttpRequest, HttpContextResponse> {
    private Logger logger = LoggerFactory.getLogger(MicrobootHttpCodec.class);
    private ServerConfig serverConfig;

    public MicrobootHttpCodec(ServerConfig serverConfig){
        this.serverConfig = serverConfig;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, HttpContextResponse response, List<Object> out) throws Exception {
        //文件
        DefaultHttpResponse httpResponse = new DefaultHttpResponse(response.getVersion(), response.getStatus());
        httpResponse.headers().clear();
        httpResponse.headers().add(response.headers());
        //out add header
        out.add(httpResponse);

        //chunked
        if (response.getFile() != null) {
            RandomAccessFile raf = new RandomAccessFile(response.getFile(), "r");
            long fileLength = raf.length();
            //filename
            httpResponse.headers().add(HttpHeaderName.TRANSFER_ENCODING, "chunked");
            packageFileHeaders(httpResponse, response.getFile());

            out.add(new HttpChunkedInput(new ChunkedFile(raf, 0, fileLength, serverConfig.getChunkSize())));
            out.add(LastHttpContent.EMPTY_LAST_CONTENT);
        }else if(response.getInputStream() != null){
            httpResponse.headers().add(HttpHeaderName.TRANSFER_ENCODING, "chunked");
            out.add(httpResponse);
            out.add(new HttpChunkedInput(new ChunkedStream(response.getInputStream(),serverConfig.getChunkSize())));
            out.add(LastHttpContent.EMPTY_LAST_CONTENT);
            return;
        }else{ //normal
            if(response.content()!=null) {
                out.add(new DefaultHttpContent(response.content()));
            }
        }
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, FullHttpRequest msg, List<Object> out) throws Exception {
        if (msg.decoderResult().isFailure()) {
            DefaultFullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST);
            if (logger.isDebugEnabled()) {
                logger.debug("illegal http,address={}", ctx.channel().remoteAddress());
            }
            ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
            return;
        }
        HttpContextRequest request = new HttpContextRequest(msg, serverConfig.getCharset());
        out.add(request);
    }


    private void packageFileHeaders(HttpResponse response, File fileToCache) {
        if(!response.headers().contains(HttpHeaderName.DATE)) {
            response.headers().set(HttpHeaderName.DATE, getDateFormatter().format(System.currentTimeMillis()));
        }
        if(!response.headers().contains(HttpHeaderName.CONTENT_TYPE)) {
            response.headers().set(HttpHeaderName.CONTENT_TYPE, MediaType.getContentType(fileToCache.getName()));
        }
        if(!response.headers().contains(HttpHeaderName.LAST_MODIFIED)) {
            response.headers().set(HttpHeaderName.LAST_MODIFIED, getDateFormatter().format(new Date(fileToCache.lastModified())));
        }
    }

    private SimpleDateFormat getDateFormatter(){
        SimpleDateFormat dateFormatter = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);
        dateFormatter.setTimeZone(TimeZone.getTimeZone("GMT"));
        return dateFormatter;
    }
}
