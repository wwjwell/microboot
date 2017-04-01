package com.zhuanglide.micrboot.mvc.resolver;

import com.zhuanglide.micrboot.mvc.ApiMethodParam;
import com.zhuanglide.micrboot.http.HttpRequest;
import com.zhuanglide.micrboot.http.HttpResponse;

public interface ApiMethodParamResolver {

    boolean support(ApiMethodParam apiMethodParam);

    Object getParamObject(ApiMethodParam apiMethodParam, HttpRequest request, HttpResponse response) throws Exception;
}
