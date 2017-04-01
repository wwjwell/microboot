package com.zhuanglide.micrboot.mvc.resolver.param;

import com.zhuanglide.micrboot.mvc.ApiMethodParam;
import com.zhuanglide.micrboot.http.HttpRequest;
import com.zhuanglide.micrboot.http.HttpResponse;
import com.zhuanglide.micrboot.mvc.resolver.ApiMethodParamResolver;

public class ApiMethodParamCommandContextResolver implements ApiMethodParamResolver {

    @Override
    public boolean support(ApiMethodParam apiMethodParam) {
        return apiMethodParam.getParamType().equals(HttpRequest.class);
    }

    @Override
    public Object getParamObject(ApiMethodParam apiMethodParam, HttpRequest context, HttpResponse response) {
        if (support(apiMethodParam)) {
            return context;
        }
        return null;
    }
}
