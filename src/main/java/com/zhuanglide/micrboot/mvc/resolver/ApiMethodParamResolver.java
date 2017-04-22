package com.zhuanglide.micrboot.mvc.resolver;

import com.zhuanglide.micrboot.mvc.ApiMethodParam;
import com.zhuanglide.micrboot.http.HttpContextRequest;
import com.zhuanglide.micrboot.http.HttpContextResponse;

public interface ApiMethodParamResolver {

    boolean support(ApiMethodParam apiMethodParam);

    Object getParamObject(ApiMethodParam apiMethodParam, HttpContextRequest request, HttpContextResponse response) throws Exception;
}
