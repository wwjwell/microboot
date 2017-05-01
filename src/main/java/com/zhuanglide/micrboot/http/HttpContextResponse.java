package com.zhuanglide.micrboot.http;

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
    private String content;
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

    public void addHeader(String name, String value) {
        httpResponse.headers().add(name, value);
    }

    public void addCookie(Cookie cookie){
        httpResponse.headers().add(HttpHeaderNames.SET_COOKIE, ServerCookieEncoder.STRICT.encode(cookie));
    }

    public void setCharset(Charset charset) {
        this.charset = charset;
    }

    public void setContent(String content){
        this.content = content;
        httpResponse.content().writeBytes(content.getBytes(charset));
    }
    public String getContent(){return content;}


    public void setContent(byte[] bytes){
        httpResponse.content().writeBytes(Unpooled.copiedBuffer(bytes));
    }

    public void setFile(String fileName, byte[] bytes){
        httpResponse.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/html; charset="+charset.displayName());
        if (null == fileName || fileName.length()==0) {
            httpResponse.headers().set(HttpHeaderNames.CONTENT_DISPOSITION, "attachment;filename=\""+fileName+"\"");
        }
        httpResponse.content().writeBytes(bytes);
    }


    public FullHttpResponse getHttpResponse(){
        return httpResponse;
    }


}
