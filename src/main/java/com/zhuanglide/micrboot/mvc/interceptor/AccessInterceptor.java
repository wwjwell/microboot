package com.zhuanglide.micrboot.mvc.interceptor;

import com.zhuanglide.micrboot.http.HttpRequest;
import com.zhuanglide.micrboot.http.HttpResponse;
import com.zhuanglide.micrboot.mvc.ApiCommandMapping;
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
    protected int order = Ordered.HIGHEST_PRECEDENCE;

    public final static String ATTR_REQ_START_TIME = "REQ_START_TIME";
    public final static String ATTR_REQ_SYS_PARAMS = "REQ_SYS_PARAMS";

    @Override
    public boolean preDispatch(HttpRequest request, HttpResponse response) {
        request.addAttachment(ATTR_REQ_START_TIME, System.currentTimeMillis());
        return true;
    }

    @Override
    public void postHandler(ApiCommandMapping mapping, HttpRequest request, HttpResponse response){
        StringBuffer params = new StringBuffer();
        if (null != request.getRequestParamsMap() && !request.getRequestParamsMap().isEmpty()) {
            for (Map.Entry<String, List<String>> paramEntry : request.getRequestParamsMap().entrySet()) {
                params.append(paramEntry.getKey()).append(":").append(join(paramEntry.getValue(), ",")).append(";");
            }
            if (params.length() > 1)
                params = params.deleteCharAt(params.length() - 1);
        }
        request.addAttachment(ATTR_REQ_SYS_PARAMS,params.toString());
    }

    @Override
    public void afterHandle(ApiCommandMapping mapping, Object modelView, HttpRequest request, HttpResponse response, Throwable throwable) {
        try {
            long startTime = (Long) request.getAttachment(ATTR_REQ_START_TIME);
            String params = (String) request.getAttachment(ATTR_REQ_SYS_PARAMS);
            accessLogger.info("clientIp={}\"|\"url={}\"|\"{}\"|\"params=[{}]\"|\"result={}\"|\"cost={}ms",
                              request.getAddress(),
                              request.getRequestUrl(),
                              headers2str(request),
                              params,
                              response.getContent(),
                              (System.currentTimeMillis() - startTime));
        } catch (Exception ex) {
            logger.error("", ex);
        }
    }

    private String headers2str(HttpRequest request) {
        StringBuffer headerString = new StringBuffer();
        if (null != request.getHeaders()) {
            for (String s : request.getHeaders().names()) {
                headerString.append(s)
                            .append(":")
                            .append(request.getHeaders().getAsString(s))
                            .append(";");
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
}