package com.zhuanglide.micrboot.test;

import com.zhuanglide.micrboot.mvc.ApiMethodMapping;
import com.zhuanglide.micrboot.http.HttpContextRequest;
import com.zhuanglide.micrboot.http.HttpContextResponse;
import com.zhuanglide.micrboot.mvc.interceptor.AbstractApiInterceptor;

/**
 * Created by wwj on 17/3/22.
 */
public class TestInterceptor extends AbstractApiInterceptor {
    @Override
    public boolean preDispatch(HttpContextRequest request, HttpContextResponse response) {
        System.out.println("TestInterceptor preDispatch url="+request.getRequestUrl());
        return super.preDispatch(request, response);
    }

    @Override
    public void postHandler(ApiMethodMapping mapping, HttpContextRequest request, HttpContextResponse response) {
        System.out.println("TestInterceptor postHandler url="+request.getRequestUrl());
        super.postHandler(mapping, request, response);
    }

    @Override
    public void afterHandle(ApiMethodMapping mapping, Object modelView, HttpContextRequest request, HttpContextResponse response, Throwable throwable) {
        System.out.println("TestInterceptor afterHandle url="+request.getRequestUrl());
        super.afterHandle(mapping, modelView, request, response, throwable);
    }
}
