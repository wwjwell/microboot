package com.zhuanglide.micrboot.http;


import com.zhuanglide.micrboot.util.IMApiUtils;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.multipart.FileUpload;

import java.io.Serializable;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * HTTP 请求上下方文封装
 */
public class HttpRequest implements Serializable {
    private static final long serialVersionUID = -2844694054296931417L;
    private long time;      //请求时间
    private String address;
    private String httpMethod;  //GET POST DELETE
    private String requestUrl;  //请求uri
    private HttpHeaders headers;  //header
    private Map<String, List<String>> requestParamsMap; //参数
    private Map<String,FileUpload> requestFiles;
    private FullHttpRequest request;    //Base request
    private Map<String,Cookie> cookies; //request cookies
    private Channel channel;
    private Map<String,Object> attachment;

    public HttpRequest(FullHttpRequest request, Channel channel) {
        this.time = System.currentTimeMillis();
        this.request = request;
        this.headers = request.headers();
        this.httpMethod = request.method().name().toUpperCase();
        this.requestUrl = request.uri();
        if(requestUrl!=null) {
            int idx = requestUrl.indexOf("?");
            if (idx > 0) {
                requestUrl = requestUrl.substring(0, idx);
            }
            if(this.requestUrl.endsWith("/")){
                requestUrl += requestUrl.substring(0, requestUrl.length());
            }
        }else{
            requestUrl = "";
        }
        IMApiUtils.fillParamsMap(request, this);     //init http params
        IMApiUtils.fillCookies(request, this);      //init http cookie
        this.channel = channel;
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


    public void setHeader(String name, String value) {
        headers.add(name, value);
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

    public String getAddress() {
        if(null == address) {
            address = request.headers().get("X-Forwarded-For");
            if(address == null || address.length() == 0 || "unknown".equalsIgnoreCase(address)) {
                address = request.headers().get("Proxy-Client-IP");
            }
            if(address == null || address.length() == 0 || "unknown".equalsIgnoreCase(address)) {
                address = request.headers().get("Proxy-Client-IP");
            }
            if(address == null || address.length() == 0 || "unknown".equalsIgnoreCase(address)) {
                address = request.headers().get("WL-Proxy-Client-IP");
            }
            if (address == null || address.length() == 0 || "unknown".equalsIgnoreCase(address)) {
                address = ((InetSocketAddress) channel.remoteAddress()).getAddress().getHostAddress();
            }
        }
        return address;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
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
    public HttpRequest clone() throws CloneNotSupportedException {
        HttpRequest _request = new HttpRequest(this.request, this.channel);
        return _request;
    }
}
