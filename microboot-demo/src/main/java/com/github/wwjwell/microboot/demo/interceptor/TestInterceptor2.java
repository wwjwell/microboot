package com.github.wwjwell.microboot.demo.interceptor;

import com.github.wwjwell.microboot.http.HttpContextRequest;
import com.github.wwjwell.microboot.http.HttpContextResponse;
import com.github.wwjwell.microboot.mvc.ApiMethodMapping;
import com.github.wwjwell.microboot.mvc.interceptor.AbstractApiInterceptor;

/**
 * Created by wwj on 2017/4/1.
 */
public class TestInterceptor2 extends AbstractApiInterceptor {
    @Override
    public boolean preDispatch(HttpContextRequest request, HttpContextResponse response) {
        System.out.println("TestInterceptor2 preDispatch url="+request.getRequestUrl());
        return super.preDispatch(request, response);
    }

    @Override
    public boolean postHandler(ApiMethodMapping mapping, HttpContextRequest request, HttpContextResponse response) {
        System.out.println("TestInterceptor2 postHandler url="+request.getRequestUrl());
        return super.postHandler(mapping, request, response);
    }

    @Override
    public void afterHandle(ApiMethodMapping mapping, Object modelView, HttpContextRequest request, HttpContextResponse response, Throwable throwable) {
        System.out.println("TestInterceptor2 afterHandle url="+request.getRequestUrl());
        super.afterHandle(mapping, modelView, request, response, throwable);
    }
}
