package com.zhuanglide.micrboot.util;

import com.zhuanglide.micrboot.http.HttpContextRequest;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;
import io.netty.handler.codec.http.multipart.Attribute;
import io.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
import io.netty.handler.codec.http.multipart.FileUpload;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import io.netty.handler.codec.http.multipart.MemoryFileUpload;
import io.netty.util.CharsetUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IMApiUtils {
    /**
     * 填充http参数
     *
     * @param request
     * @return
     */
    public static void fillParamsMap(FullHttpRequest request, HttpContextRequest context) {
        Map<String, List<String>> requestParamsMap = new HashMap();
        Map<String,FileUpload> requestFiles = new HashMap<String,FileUpload>();
        QueryStringDecoder decoderQuery = new QueryStringDecoder(request.uri(),CharsetUtil.UTF_8);
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
                        attribute.setCharset(CharsetUtil.UTF_8);
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
                e.printStackTrace();
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
        String value = request.headers().get("Cookie");
        if (null != value && value.length()>0){
            for (Cookie cookie : ServerCookieDecoder.STRICT.decode(value)) {
                cookies.put(cookie.name(), cookie);
            }
        }
        context.setCookies(cookies);
    }


    /**
     * 左匹配，右边以*结尾代表模糊匹配
     *
     * @param srcMethodName
     * @param wildcardMethodName
     * @return
     */
    public static boolean leftMatch(String srcMethodName, String wildcardMethodName) {
        if (srcMethodName == null && wildcardMethodName == null) {
            return true;//全是null，确实匹配。
        }
        if (srcMethodName == null || wildcardMethodName == null) {
            return false;//有一个是null，不匹配。
        }
        if (srcMethodName.length() == 0 && wildcardMethodName.length() == 0) {
            return true;//全是空串，确实匹配。
        }
        if (wildcardMethodName.length() == 0) {
            return false;//匹配串是空串，不匹配。
        }
        if (wildcardMethodName.equals("*")) {
            return true;//全匹配
        }
        if (wildcardMethodName.endsWith("*")) {
            wildcardMethodName = wildcardMethodName.substring(0, wildcardMethodName.length() - 1);
            return srcMethodName.startsWith(wildcardMethodName);
        }
        return srcMethodName.equalsIgnoreCase(wildcardMethodName);
    }
}
