package com.zhuanglide.micrboot.test;

import com.zhuanglide.micrboot.http.HttpRequest;
import com.zhuanglide.micrboot.http.HttpResponse;
import com.zhuanglide.micrboot.mvc.ApiCommandMapping;
import com.zhuanglide.micrboot.mvc.interceptor.AbstractApiInterceptor;

/**
 * Created by wwj on 2017/4/1.
 */
public class TestInterceptor2 extends AbstractApiInterceptor {
    @Override
    public boolean preDispatch(HttpRequest request, HttpResponse response) {
        System.out.println("TestInterceptor2 preDispatch url="+request.getRequestUrl());
        return super.preDispatch(request, response);
    }

    @Override
    public void postHandler(ApiCommandMapping mapping, HttpRequest request, HttpResponse response) {
        System.out.println("TestInterceptor2 postHandler url="+request.getRequestUrl());
        super.postHandler(mapping, request, response);
    }

    @Override
    public void afterHandle(ApiCommandMapping mapping, Object modelView, HttpRequest request, HttpResponse response, Throwable throwable) {
        System.out.println("TestInterceptor2 afterHandle url="+request.getRequestUrl());
        super.afterHandle(mapping, modelView, request, response, throwable);
    }
}
