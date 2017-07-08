package com.zhuanglide.microboot.util;

import com.zhuanglide.microboot.http.HttpContextRequest;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;
import io.netty.handler.codec.http.multipart.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HttpUtils {
    private static Logger logger = LoggerFactory.getLogger(HttpUtils.class);
    /**
     * 填充http参数
     *
     * @param request
     * @return
     */
    public static void fillParamsMap(FullHttpRequest request, HttpContextRequest context, Charset charset) {
        Map<String, List<String>> requestParamsMap = new HashMap<String, List<String>>();
        Map<String,FileUpload> requestFiles = new HashMap<String,FileUpload>();
        QueryStringDecoder decoderQuery = new QueryStringDecoder(request.uri(),charset);
        Map<String, List<String>> uriAttributes = decoderQuery.parameters();
        for (Map.Entry<String, List<String>> attr : uriAttributes.entrySet()) {
            requestParamsMap.put(attr.getKey(), attr.getValue());
        }
        if (request.method().equals(HttpMethod.POST)) {
            HttpPostRequestDecoder decoder = new HttpPostRequestDecoder(new DefaultHttpDataFactory(false), request);
            try{
                List<InterfaceHttpData> postList = decoder.getBodyHttpDatas();
                for (InterfaceHttpData data : postList) {
                    String name = data.getName();
                    if (InterfaceHttpData.HttpDataType.Attribute == data.getHttpDataType()) {
                        Attribute attribute = (Attribute) data;
                        attribute.setCharset(charset);
                        String value = attribute.getValue();
                        List<String> valueList = requestParamsMap.get(name);
                        if(null == valueList) {
                            valueList = new ArrayList<String>();
                            requestParamsMap.put(name, valueList);
                        }
                        valueList.add(value);
                    }else if(InterfaceHttpData.HttpDataType.FileUpload == data.getHttpDataType()){
                        MemoryFileUpload fileUpload = (MemoryFileUpload) data;
                        requestFiles.put(name, fileUpload);
                    }

                }

            }catch (Exception e){
                logger.error("parse post param failed", e);
            }
        }
        context.setRequestParamsMap(requestParamsMap);
        if (!requestFiles.isEmpty()) {
            context.setRequestFiles(requestFiles);
        }
    }

    /**
     * 填充Cookie
     * @param request
     * @param context
     */
    public static void fillCookies(FullHttpRequest request, HttpContextRequest context) {
        Map<String,Cookie> cookies = new HashMap<String,Cookie>();
        String value = request.headers().get(HttpHeaderNames.COOKIE);
        if (null != value && value.length()>0){
            for (Cookie cookie : ServerCookieDecoder.STRICT.decode(value)) {
                cookies.put(cookie.name(), cookie);
            }
        }
        context.setCookies(cookies);
    }


    /**
     * 组装优化路径 [/a,/b]->/a/b/
     * [a,/b] -> /a/b/
     * [/a/,b/] -> /a/b/
     * [a,b] -> /a/b/
     * @param path
     * @return
     */
    public static String joinOptimizePath(String ... path ) {
        StringBuffer _url = new StringBuffer();
        _url.append("/");
        if(null != path) {
            for (String url : path) {
                if (null != url) {
                    _url.append(url).append("/");
                }
            }
        }
        int idx = _url.indexOf("//");
        while (idx != -1) {
            _url = _url.deleteCharAt(idx);
            idx = _url.indexOf("//");
        }
        return _url.toString();
    }
}
