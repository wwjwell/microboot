package com.zhuanglide.micrboot.http;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.ServerCookieEncoder;

import java.nio.charset.Charset;

/**
 * Created by wwj on 17/3/2.
 */
public class HttpContextResponse {
    private FullHttpResponse httpResponse;
    private Charset charset;
    public HttpContextResponse(HttpVersion version, HttpResponseStatus status, Charset charset){
        httpResponse = new DefaultFullHttpResponse(version, status);
        this.charset = charset;
    }

    public HttpResponseStatus getStatus() {
        return httpResponse.status();
    }
    public void setStatus(HttpResponseStatus status) {
        httpResponse.setStatus(status);
    }

    /**
     * header
     */
    public void addHeader(CharSequence name, Object value) {
        httpResponse.headers().add(name, value);
    }

    public boolean containsHeader(CharSequence name) {
        return httpResponse.headers().contains(name);
    }

    public String getHeader(CharSequence name) {
        return httpResponse.headers().get(name);
    }

    /**
     * cookie
     */
    public void addCookie(Cookie cookie){
        httpResponse.headers().add(HttpHeaderNames.SET_COOKIE, ServerCookieEncoder.STRICT.encode(cookie));
    }

    public Charset getCharset() {
        return charset;
    }

    public void setCharset(Charset charset) {
        this.charset = charset;
    }

    public String getContent(){
        ByteBuf content = httpResponse.content();
        if (null == content) {
            return null;
        }
        content.markReaderIndex();
        int len = content.readableBytes();
        CharSequence _content = content.readCharSequence(len, charset);
        content.resetReaderIndex();
        return _content.toString();
    }
    public void setContent(String content){
        setContent(content.getBytes(charset));
    }

    public void setContent(byte[] bytes){
        httpResponse.content().writeBytes(Unpooled.copiedBuffer(bytes));
    }

    public FullHttpResponse getHttpResponse(){
        return httpResponse;
    }


}
