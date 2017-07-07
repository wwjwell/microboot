package com.zhuanglide.microboot.mvc.resolver;

import com.zhuanglide.microboot.mvc.ApiMethodMapping;
import com.zhuanglide.microboot.mvc.ApiMethodParam;
import com.zhuanglide.microboot.http.HttpContextRequest;
import com.zhuanglide.microboot.http.HttpContextResponse;
import org.springframework.core.Ordered;

public interface ApiMethodParamResolver extends Ordered{
    String ATTACHMENT_API_METHOD_PATH_VARIABLE_KEY = "ATTACHMENT_API_METHOD_PATH_VARIABLE_KEY";
    boolean support(ApiMethodParam apiMethodParam);

    Object getParamObject(ApiMethodParam apiMethodParam, HttpContextRequest request, HttpContextResponse response) throws Exception;

    void prepare(ApiMethodMapping mapping, HttpContextRequest request, Object... args);
}
