package com.zhuanglide.microboot.mvc.interceptor;

import com.zhuanglide.microboot.constants.Constants;
import com.zhuanglide.microboot.http.HttpContextRequest;
import com.zhuanglide.microboot.http.HttpContextResponse;
import com.zhuanglide.microboot.mvc.ApiMethodMapping;
import io.netty.handler.codec.http.HttpHeaderNames;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Created by wwj on 16/11/9.
 */
public class AccessInterceptor extends AbstractApiInterceptor {
    private Logger logger = LoggerFactory.getLogger(AccessInterceptor.class);
    private String accessLoggerName = this.getClass().getName();
    private Logger accessLogger = LoggerFactory.getLogger(accessLoggerName);
    private boolean appendHeader = false;       //是否展示header内容
    private boolean appendParam = true;         //是否展示param内容
    private boolean appendResponse = false;     //是否展示response内容
    private String split = "|,|";
    protected int order = Ordered.HIGHEST_PRECEDENCE + 100;

    public final static String ATTR_REQ_START_TIME = "REQ_START_TIME";
    public final static String ATTR_REQ_SYS_PARAMS = "REQ_SYS_PARAMS";

    @Override
    public boolean preDispatch(HttpContextRequest request, HttpContextResponse response) {
        request.addAttachment(ATTR_REQ_START_TIME, System.currentTimeMillis());
        return true;
    }

    @Override
    public boolean postHandler(ApiMethodMapping mapping, HttpContextRequest request, HttpContextResponse response){
        StringBuffer params = new StringBuffer();
        if (null != request.getRequestParamsMap() && !request.getRequestParamsMap().isEmpty()) {
            for (Map.Entry<String, List<String>> paramEntry : request.getRequestParamsMap().entrySet()) {
                params.append(paramEntry.getKey()).append(":").append(join(paramEntry.getValue(), ",")).append(";");
            }
            if (params.length() > 1)
                params = params.deleteCharAt(params.length() - 1);
        }
        request.addAttachment(ATTR_REQ_SYS_PARAMS,params.toString());
        return true;
    }

    @Override
    public void afterCompletion(ApiMethodMapping mapping, HttpContextRequest request, HttpContextResponse response, Throwable throwable) {
        try {
            long startTime = (Long) request.getAttachment(ATTR_REQ_START_TIME);
            String params = (String) request.getAttachment(ATTR_REQ_SYS_PARAMS);
            StringBuffer log = new StringBuffer();
            log.append(split).append("reqId=").append(request.getAttachment(Constants.REQ_ID));
            log.append(split).append(request.getHttpMethod());
            log.append(split).append(request.getHttpVersion().text());
            log.append(split).append(request.getHeader(HttpHeaderNames.HOST));
            log.append(split).append("url=").append(request.getRequestUrl());
            if (appendHeader) {
                log.append(split).append(headers2str(request));
            }
            if(isAppendParam()) {
                log.append(split).append("params=").append(params);
            }
            log.append(split).append("status=").append(response.getStatus().code());
            if(isAppendResponse()) {
                log.append(split).append("response=").append(response.getContent());
            }
            log.append(split).append("cost=").append((System.currentTimeMillis() - startTime)).append("ms");
            accessLogger.info(log.toString());
        } catch (Exception ex) {
            logger.error("", ex);
        }
    }

    private String headers2str(HttpContextRequest request) {
        StringBuffer headerString = new StringBuffer();
        headerString.append("header=");
        if (null != request.getHeaders()) {
            for (String s : request.getHeaders().names()) {
                headerString.append(s).append(":").append(request.getHeaders().getAsString(s)).append(";");
            }
            if (headerString.length() > 0) {
                headerString =  headerString.deleteCharAt(headerString.length() - 1);
            }
        }
        return headerString.toString();
    }

    @Override
    public int getOrder() {
        return order;
    }

    public void setAccessLoggerName(String accessLoggerName) {
        this.accessLoggerName = accessLoggerName;
    }

    private String join(Collection<String> array, String split) {
        StringBuffer sb = new StringBuffer();
        for (String s : array) {
            sb.append(s) .append(split);
        }
        if (sb.length() > split.length()) {
            sb.deleteCharAt(sb.length() - split.length());
        }
        return sb.toString();
    }

    public void setAppendHeader(boolean appendHeader) {
        this.appendHeader = appendHeader;
    }

    public void setSplit(String split) {
        this.split = split;
    }

    public String getAccessLoggerName() {
        return accessLoggerName;
    }

    public Logger getAccessLogger() {
        return accessLogger;
    }

    public void setAccessLogger(Logger accessLogger) {
        this.accessLogger = accessLogger;
    }

    public boolean isAppendHeader() {
        return appendHeader;
    }

    public boolean isAppendParam() {
        return appendParam;
    }

    public void setAppendParam(boolean appendParam) {
        this.appendParam = appendParam;
    }

    public boolean isAppendResponse() {
        return appendResponse;
    }

    public void setAppendResponse(boolean appendResponse) {
        this.appendResponse = appendResponse;
    }
}