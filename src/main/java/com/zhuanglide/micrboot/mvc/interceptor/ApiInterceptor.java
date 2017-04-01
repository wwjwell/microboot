package com.zhuanglide.micrboot.mvc.interceptor;

import com.zhuanglide.micrboot.mvc.ApiCommandMapping;
import com.zhuanglide.micrboot.http.HttpRequest;
import com.zhuanglide.micrboot.http.HttpResponse;
import org.springframework.core.Ordered;

/**
 * api拦截器
 * Created by wwj on 16/7/19.
 */
public interface ApiInterceptor extends Ordered{
    /**
     * 作用于url匹配分发之前，甚至可以url以更改invoke的method
     * @param request
     * @param response
     * @return
     */
    boolean preDispatch(HttpRequest request, HttpResponse response);

    /**
     * 作用于invoke之前，可以更改请求参数的值，
     * @param mapping
     * @param request
     * @param response
     */
    void postHandler(ApiCommandMapping mapping, HttpRequest request, HttpResponse response);

    /**
     * 作用于invoke之后，可以更改返回值
     * @param mapping
     * @param modelView
     * @param request
     * @param response
     * @param throwable
     */
    void afterHandle(ApiCommandMapping mapping, Object modelView, HttpRequest request, HttpResponse response, Throwable throwable);

}
