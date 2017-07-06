package com.zhuanglide.micrboot.http;


import com.zhuanglide.micrboot.util.HttpUtils;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
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
    private FullHttpRequest request;    //Base request
    private Map<String,Cookie> cookies; //request cookies
    private Map<String,Object> attachment;

    public HttpContextRequest(FullHttpRequest request,Charset charset) {
        this.time = System.currentTimeMillis();
        this.request = request;
        this.charset = charset;
        this.headers = request.headers();
        this.httpVersion = request.protocolVersion();
        this.httpMethod = request.method().name().toUpperCase();
        this.requestUrl = request.uri();
        if(requestUrl!=null) {
            int idx = requestUrl.indexOf("?");
            if (idx > 0) {
                requestUrl = requestUrl.substring(0, idx);
            }
        }
        requestUrl = HttpUtils.joinOptimizePath(requestUrl);
        HttpUtils.fillParamsMap(request, this, charset);     //init http params
        HttpUtils.fillCookies(request, this);      //init http cookie
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

    public FullHttpRequest getRequest() {
        return request;
    }

    public void setRequest(FullHttpRequest request) {
        this.request = request;
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
        HttpContextRequest _request = new HttpContextRequest(this.request,this.charset);
        _request.requestParamsMap.putAll(this.requestParamsMap);
        _request.cookies.putAll(this.cookies);
        if (null != this.attachment) {
            if (_request.attachment == null) {
                _request.attachment = new HashMap<String, Object>();
            }
            _request.attachment.putAll(this.attachment);
        }
        _request.body = this.getBody();
        return _request;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
