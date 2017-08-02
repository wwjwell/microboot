package com.github.wwjwell.microboot.http;


import com.github.wwjwell.microboot.util.HttpUtils;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.multipart.FileUpload;

import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * HTTP 请求上下方文封装
 */
public class HttpContextRequest implements Serializable {
    private static final long serialVersionUID = -2844694054296931417L;
    private long time;      //请求时间
    private String httpMethod;  //GET POST DELETE
    private String requestUrl;  //请求uri
    private HttpVersion httpVersion;
    private HttpHeaders headers;  //header
    private Map<String, List<String>> requestParamsMap; //参数
    private Map<String,FileUpload> requestFiles;
    private String body;
    private Charset charset;
    private Map<String,Cookie> cookies; //request cookies
    private Map<String,Object> attachment;

    public HttpContextRequest(){
    }

    public HttpContextRequest(HttpVersion httpVersion, HttpHeaders httpHeaders, Charset charset) {
        this.time = System.currentTimeMillis();
        this.charset = charset;
        this.headers = httpHeaders;
        this.httpVersion = httpVersion;
        requestUrl = HttpUtils.joinOptimizePath(requestUrl);
        this.getBody();
    }

    public HttpVersion getHttpVersion() {
        return httpVersion;
    }

    public void setHttpVersion(HttpVersion httpVersion) {
        this.httpVersion = httpVersion;
    }

    public String getParameter(String name){
        List<String> values = requestParamsMap.get(name);
        if(null != values && values.size()>0){
            return values.get(0);
        }
        return null;
    }

    public void setCharset(Charset charset) {
        this.charset = charset;
    }

    public Charset getCharset() {
        return charset;
    }

    public void addParameter(String name, String value) {
        requestParamsMap.put(name, Arrays.asList(value));
    }

    public List<String> getParameters(String name){
        return requestParamsMap.get(name);
    }


    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    public String getRequestUrl() {
        return requestUrl;
    }

    public void setRequestUrl(String requestUrl) {
        this.requestUrl = requestUrl;
    }

    public HttpHeaders getHeaders() {
        return headers;
    }

    public void setHeader(CharSequence name, Object value) {
        headers.add(name, value);
    }
    public String getHeader(CharSequence name) {
        return headers.get(name);
    }

    public Map<String, List<String>> getRequestParamsMap() {
        return requestParamsMap;
    }

    public void setRequestParamsMap(Map<String, List<String>> requestParamsMap) {
        this.requestParamsMap = requestParamsMap;
    }

    public FileUpload getFileUpload(String name) {
        if (null == requestFiles) {
            return null;
        }
        return requestFiles.get(name);
    }

    public Map<String, Cookie> getCookies() {
        return cookies;
    }

    public void setCookies(Map<String, Cookie> cookies) {
        this.cookies = cookies;
    }

    public Cookie getCookie(String name){
        if (null != cookies) {
            return cookies.get(name);
        }
        return null;
    }

    public Map<String, FileUpload> getRequestFiles() {
        return requestFiles;
    }

    public void setRequestFiles(Map<String, FileUpload> requestFiles) {
        this.requestFiles = requestFiles;
    }

    public void addAttachment(String key,Object value) {
        if (null == this.attachment) {
            this.attachment = new HashMap<String,Object>();
        }
        this.attachment.put(key, value);
    }

    public Object getAttachment(String key) {
        if (null == attachment) {
            return null;
        }
        return attachment.get(key);
    }

    @Override
    public HttpContextRequest clone() throws CloneNotSupportedException {
        return copy();
    }

    public HttpContextRequest copy() throws CloneNotSupportedException {
        HttpContextRequest _request = new HttpContextRequest();
        _request.charset = this.charset;
        _request.headers = this.headers;
        _request.httpVersion = this.httpVersion;
        _request.httpMethod = this.httpMethod;
        _request.requestUrl = this.requestUrl;
        _request.body = this.getBody();
        _request.requestParamsMap = new HashMap<String, List<String>>(this.requestParamsMap);
        _request.cookies = new HashMap<String, Cookie>(this.cookies);
        _request.attachment = new HashMap<String, Object>(this.attachment);
        return _request;
    }

    public String getBody() {
        return body;
    }
    public void setBody(String body) {
        this.body = body;
    }
}
