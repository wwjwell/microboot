package com.github.wwjwell.microboot.http;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.buffer.UnpooledHeapByteBuf;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.ServerCookieEncoder;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.Charset;

/**
 * Created by wwj on 17/3/2.
 */
public class HttpContextResponse {
    private HttpResponseStatus status = HttpResponseStatus.OK;
    private HttpVersion version = HttpVersion.HTTP_1_1;
    private Charset charset;
    private HttpHeaders headers;
    private File file;
    private InputStream inputStream;
    private ByteBuf content;
    public HttpContextResponse(HttpVersion version, HttpResponseStatus status, Charset charset){
        this.version = version;
        this.status = status;
        this.charset = charset;
        this.headers = new DefaultHttpHeaders(true);
    }

    public HttpVersion getVersion() {
        return version;
    }

    public void setVersion(HttpVersion version) {
        this.version = version;
    }

    public HttpHeaders headers() {
        return headers;
    }

    public void setHeaders(HttpHeaders headers) {
        this.headers = headers;
    }

    public HttpResponseStatus getStatus() {
        return status;
    }
    public void setStatus(HttpResponseStatus status) {
        this.status = status;
    }

    /**
     * header
     */
    public void addHeader(CharSequence name, Object value) {
        headers.add(name, value);
    }

    public boolean containsHeader(CharSequence name) {
        return headers.contains(name);
    }

    public String getHeader(CharSequence name) {
        return headers.get(name);
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public void setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    /**
     * cookie
     */
    public void addCookie(Cookie cookie){
        headers.add(HttpHeaderNames.SET_COOKIE, ServerCookieEncoder.STRICT.encode(cookie));
    }

    public Charset getCharset() {
        return charset;
    }

    public void setCharset(Charset charset) {
        this.charset = charset;
    }

    public String getContent(){
        if (null == content) {
            return null;
        }
        content.markReaderIndex();
        String body = content.toString(charset);
        content.resetReaderIndex();
        return body;
    }

    public ByteBuf content(){
        return content;
    }
    public void setContent(String content){
        setContent(content.getBytes(charset));
    }


    public void setContent(byte[] bytes){
        if(null != bytes){
            if (null == content) {
                content = Unpooled.buffer(bytes.length);
            }else {
                content.clear();
            }
            content.writeBytes(bytes);
        }
    }

    public void writeToContent(byte[] bytes) {
        if(null != bytes){
            if (null == content) {
                content = Unpooled.buffer(bytes.length);
            }
            content.writeBytes(bytes);
        }
    }
}
