package com.github.wwjwell.microboot;

import com.github.wwjwell.microboot.http.*;
import com.github.wwjwell.microboot.http.HttpVersion;
import com.github.wwjwell.microboot.util.HttpUtils;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http2.HttpConversionUtil;
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

    /**
     *HttpContextResponse - > HttpResponse + httpContent(if trunked)
     * @param ctx
     * @param response
     * @param out
     * @throws Exception
     */
    @Override
    protected void encode(ChannelHandlerContext ctx, HttpContextResponse response, List<Object> out) throws Exception {
        io.netty.handler.codec.http.HttpVersion httpVersion = null;
        if(response.getVersion() == HttpVersion.HTTP_2){
            httpVersion = io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
        }else{
            httpVersion = io.netty.handler.codec.http.HttpVersion.valueOf(response.getVersion().text());
        }

        DefaultHttpResponse httpResponse = new DefaultHttpResponse(httpVersion, response.getStatus());
        httpResponse.headers().add(response.headers());
        //out add header
        out.add(httpResponse);
        //chunked
        if (response.getFile() != null) {
            RandomAccessFile raf = new RandomAccessFile(response.getFile(), "r");
            long fileLength = raf.length();
            //filename
            httpResponse.headers().add(HttpHeaderName.TRANSFER_ENCODING, HttpHeaderValues.CHUNKED);
            packageFileHeaders(httpResponse, response.getFile());
            out.add(new HttpChunkedInput(new ChunkedFile(raf, 0, fileLength, serverConfig.getChunkSize())));
        }else if(response.getInputStream() != null){
            httpResponse.headers().add(HttpHeaderName.TRANSFER_ENCODING, HttpHeaderValues.CHUNKED);
            out.add(new HttpChunkedInput(new ChunkedStream(response.getInputStream(),serverConfig.getChunkSize())));

        }else{ //normal
            if(response.content()!=null) {
                out.add(new DefaultHttpContent(response.content()));
            }
        }
        out.add(LastHttpContent.EMPTY_LAST_CONTENT);
    }

    /**
     * FullHttpRequest -> httpContextRequest
     * @param ctx
     * @param msg
     * @param out
     * @throws Exception
     */
    @Override
    protected void decode(ChannelHandlerContext ctx, FullHttpRequest msg, List<Object> out) throws Exception {
        if (msg.decoderResult().isFailure()) {
            DefaultFullHttpResponse response = new DefaultFullHttpResponse(io.netty.handler.codec.http.HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST);
            if (logger.isDebugEnabled()) {
                logger.debug("illegal http,address={}", ctx.channel().remoteAddress());
            }
            ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
            return;
        }
        //convert to request
        HttpVersion version = HttpVersion.valueOf(msg.protocolVersion().text());
        //HTTP 2.0
        if(msg.headers().contains(HttpConversionUtil.ExtensionHeaderNames.STREAM_ID.text())){
            version = HttpVersion.HTTP_2;
        }

        HttpContextRequest request = new HttpContextRequest(version, msg.headers(), serverConfig.getCharset());
        request.setTime(System.currentTimeMillis());
        request.setCharset(serverConfig.getCharset());
        request.setHttpMethod(msg.method().name().toUpperCase());
        String requestUrl = msg.uri();
        if(requestUrl!=null) {
            int idx = requestUrl.indexOf("?");
            if (idx > 0) {
                requestUrl = requestUrl.substring(0, idx);
            }
        }
        requestUrl = HttpUtils.joinOptimizePath(requestUrl);
        request.setRequestUrl(requestUrl);

        String body = "";
        if (null != msg.content()) {
            int len = msg.content().readableBytes();
            if (len > 0) {
                msg.content().markReaderIndex();
                byte[] bytes = new byte[len];
                msg.content().readBytes(bytes);
                body = new String(bytes, request.getCharset());
                msg.content().resetReaderIndex();
            }
        }
        request.setBody(body);
        HttpUtils.fillParamsMap(msg, request, request.getCharset());     //init http params
        HttpUtils.fillCookies(msg, request);      //init http cookie
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
