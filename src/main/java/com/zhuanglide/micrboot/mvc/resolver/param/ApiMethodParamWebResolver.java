package com.zhuanglide.micrboot.mvc.resolver.param;

import com.zhuanglide.micrboot.http.HttpContextRequest;
import com.zhuanglide.micrboot.http.HttpContextResponse;
import com.zhuanglide.micrboot.mvc.ApiMethodParam;
import com.zhuanglide.micrboot.mvc.annotation.ApiParam;
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

            if (type.equals(HttpContextRequest.class)) {
                return request;
            } else if (type.equals(HttpContextResponse.class)) {
                return response;
            }
            //文件上传
            else {
                if (type.equals(FileUpload.class)) {
                    return request.getFileUpload(paramName);
                }
                //cookie
                else if (type.equals(Cookie.class)) {
                    return request.getCookie(paramName);
                }
            }
        }
        return null;
    }
}
