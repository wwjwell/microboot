package com.github.wwjwell.microboot.mvc.resolver.param;

import com.github.wwjwell.microboot.http.HttpContextRequest;
import com.github.wwjwell.microboot.http.HttpContextResponse;
import com.github.wwjwell.microboot.mvc.ApiMethodParam;
import com.github.wwjwell.microboot.mvc.annotation.ApiParam;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.multipart.FileUpload;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

public class ApiMethodParamWebResolver extends AbstractApiMethodParamResolver {

    private static final Set<Class> BASIC_TYPE_CLASS = new HashSet<Class>(){{
        add(HttpContextRequest.class);
        add(HttpContextResponse.class);
        add(FileUpload.class);
        add(Cookie.class);
    }};


    public boolean support(ApiMethodParam apiMethodParam) {
        return BASIC_TYPE_CLASS.contains(apiMethodParam.getParamType());
    }

    public Object getParamObject(ApiMethodParam apiMethodParam, HttpContextRequest request,HttpContextResponse response) {
        if (support(apiMethodParam)) {
            Annotation[] paramAnnotations = apiMethodParam.getParamAnnotations();
            ApiParam apiParamAnnotation = null;
            String paramName = apiMethodParam.getParamName();

            if (paramAnnotations != null) {
                for (Annotation paramAnnotation : paramAnnotations) {
                    if (paramAnnotation instanceof ApiParam) {
                        apiParamAnnotation = (ApiParam) paramAnnotation;
                        paramName = apiParamAnnotation.value();
                        break;
                    }
                }
            }

            Type type = apiMethodParam.getParamType();
            Object paramValue = null;
            if (type.equals(HttpContextRequest.class)) {
                paramValue = request;
            } else if (type.equals(HttpContextResponse.class)) {
                paramValue = response;
            } else {//文件上传
                if (type.equals(FileUpload.class)) {
                    paramValue = request.getFileUpload(paramName);
                }
                //cookie
                else if (type.equals(Cookie.class)) {
                    paramValue = request.getCookie(paramName);
                }
            }

            if ( apiParamAnnotation != null
                    && apiParamAnnotation.required()
                    && paramValue == null) {
                throw new IllegalArgumentException("param=" + paramName +" is required");
            }
            return paramValue;
        }
        return null;
    }
}
