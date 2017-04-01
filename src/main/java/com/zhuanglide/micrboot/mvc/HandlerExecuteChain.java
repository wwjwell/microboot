package com.zhuanglide.micrboot.mvc;

import com.zhuanglide.micrboot.http.HttpRequest;
import com.zhuanglide.micrboot.http.HttpResponse;
import com.zhuanglide.micrboot.mvc.interceptor.ApiInterceptor;
import com.zhuanglide.micrboot.mvc.resolver.ExceptionResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ObjectUtils;

import java.util.List;

/**
 * Created by wwj on 17/3/2.
 */
public class HandlerExecuteChain {
    private Logger logger = LoggerFactory.getLogger(HandlerExecuteChain.class);
    private List<ApiInterceptor> apiInterceptors;
    private List<ExceptionResolver> exceptionResolvers;
    private ApiCommandMapping mapping;
    private Object result;
    private int interceptorIndex = -1;

    public HandlerExecuteChain(List<ApiInterceptor> apiInterceptors,List<ExceptionResolver> exceptionResolvers) {
        this.apiInterceptors = apiInterceptors;
        this.exceptionResolvers = exceptionResolvers;
    }

    public ApiCommandMapping getMapping() {
        return mapping;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    public void setMapping(ApiCommandMapping mapping) {
        this.mapping = mapping;
    }

    public boolean applyPreDispatch(HttpRequest request, HttpResponse response) throws Exception {
        if (!ObjectUtils.isEmpty(apiInterceptors)) {
            for (int i = 0; i < apiInterceptors.size(); i++) {
                ApiInterceptor interceptor = apiInterceptors.get(i);
                if (!interceptor.preDispatch(request, response)) {
                    triggerAfterCompletion(request, response, null);
                    return false;
                }
                this.interceptorIndex = i;
            }
        }
        return true;
    }

    public void applyPostHandle(HttpRequest request, HttpResponse response) throws Exception {
        if (!ObjectUtils.isEmpty(apiInterceptors)) {
            for (ApiInterceptor apiInterceptor : apiInterceptors) {
                apiInterceptor.postHandler(mapping, request, response);
            }
        }
    }

    public void triggerAfterCompletion(HttpRequest request, HttpResponse response,Throwable e)
            throws Exception {
        if (!ObjectUtils.isEmpty(apiInterceptors)) {
            for (int i = this.interceptorIndex; i >= 0; i--) {
                ApiInterceptor interceptor = apiInterceptors.get(i);
                try {
                    interceptor.afterHandle(mapping, result, request, response, e);
                } catch (Throwable ex2) {
                    logger.error("ApiInterceptor.afterCompletion threw exception", ex2);
                }
            }
        }
        if (null != e) {
            triggerException(request, response, e);
        }
    }

    /**
     * 对异常处理
     */
    public void triggerException(HttpRequest request,HttpResponse response,Throwable ex){
        if (!ObjectUtils.isEmpty(exceptionResolvers)) {
            for (ExceptionResolver exceptionResolver : exceptionResolvers) {
                exceptionResolver.resolveException(mapping, request, response, ex);
            }
        }
    }
}
