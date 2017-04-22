package com.zhuanglide.micrboot.mvc.resolver.param;

import com.zhuanglide.micrboot.mvc.ApiMethodParam;
import com.zhuanglide.micrboot.http.HttpContextRequest;
import com.zhuanglide.micrboot.http.HttpContextResponse;
import com.zhuanglide.micrboot.mvc.resolver.ApiMethodParamResolver;

public class ApiMethodParamCommandContextResolver implements ApiMethodParamResolver {

    @Override
    public boolean support(ApiMethodParam apiMethodParam) {
        return apiMethodParam.getParamType().equals(HttpContextRequest.class);
    }

    @Override
    public Object getParamObject(ApiMethodParam apiMethodParam, HttpContextRequest context, HttpContextResponse response) {
        if (support(apiMethodParam)) {
            return context;
        }
        return null;
    }
}
