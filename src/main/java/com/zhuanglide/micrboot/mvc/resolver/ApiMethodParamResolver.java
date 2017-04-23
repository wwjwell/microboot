package com.zhuanglide.micrboot.mvc.resolver;

import com.zhuanglide.micrboot.mvc.ApiMethodParam;
import com.zhuanglide.micrboot.http.HttpContextRequest;
import com.zhuanglide.micrboot.http.HttpContextResponse;
import org.springframework.core.Ordered;

public interface ApiMethodParamResolver extends Ordered{
    String ATTACHMENT_API_METHOD_PATH_VARIABLE_KEY = "ATTACHMENT_API_METHOD_PATH_VARIABLE_KEY";
    boolean support(ApiMethodParam apiMethodParam);

    Object getParamObject(ApiMethodParam apiMethodParam, HttpContextRequest request, HttpContextResponse response) throws Exception;
}
