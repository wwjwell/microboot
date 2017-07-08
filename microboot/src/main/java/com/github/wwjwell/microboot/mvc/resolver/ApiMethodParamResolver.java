package com.github.wwjwell.microboot.mvc.resolver;

import com.github.wwjwell.microboot.http.HttpContextRequest;
import com.github.wwjwell.microboot.http.HttpContextResponse;
import com.github.wwjwell.microboot.mvc.ApiMethodMapping;
import com.github.wwjwell.microboot.mvc.ApiMethodParam;
import org.springframework.core.Ordered;

public interface ApiMethodParamResolver extends Ordered{
    String ATTACHMENT_API_METHOD_PATH_VARIABLE_KEY = "ATTACHMENT_API_METHOD_PATH_VARIABLE_KEY";
    boolean support(ApiMethodParam apiMethodParam);

    Object getParamObject(ApiMethodParam apiMethodParam, HttpContextRequest request, HttpContextResponse response) throws Exception;

    void prepare(ApiMethodMapping mapping, HttpContextRequest request, Object... args);
}
