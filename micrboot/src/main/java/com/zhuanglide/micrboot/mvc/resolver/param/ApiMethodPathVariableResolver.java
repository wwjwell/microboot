package com.zhuanglide.micrboot.mvc.resolver.param;

import com.zhuanglide.micrboot.http.HttpContextRequest;
import com.zhuanglide.micrboot.http.HttpContextResponse;
import com.zhuanglide.micrboot.mvc.ApiMethodMapping;
import com.zhuanglide.micrboot.mvc.ApiMethodParam;
import com.zhuanglide.micrboot.mvc.annotation.ApiPathVariable;
import com.zhuanglide.micrboot.mvc.resolver.ApiMethodParamResolver;
import org.springframework.util.PathMatcher;

import java.lang.annotation.Annotation;
import java.util.Map;

/**
 * Created by wwj on 2017/4/23.
 */
public class ApiMethodPathVariableResolver extends AbstractApiMethodParamResolver {
    protected int order = 1;
    @Override
    public boolean support(ApiMethodParam apiMethodParam) {
        boolean hasApiPathVariableAnnotation = false;
        Annotation[] paramAnnotations = apiMethodParam.getParamAnnotations();
        if (null != paramAnnotations) {
            for (Annotation annotation : paramAnnotations) {
                if (annotation instanceof ApiPathVariable) {
                    hasApiPathVariableAnnotation = true;
                    break;
                }
            }
        }
        return hasApiPathVariableAnnotation;
    }

    @Override
    public Object getParamObject(ApiMethodParam apiMethodParam, HttpContextRequest request, HttpContextResponse response) throws Exception {
        Annotation[] paramAnnotations = apiMethodParam.getParamAnnotations();
        if (null != paramAnnotations) {
            Object attachment = request.getAttachment(ATTACHMENT_API_METHOD_PATH_VARIABLE_KEY);
            if(attachment == null || !(attachment instanceof Map)){
                return null;
            }
            Map<String,String> pathVariableMap = (Map<String,String>) attachment;
            for (Annotation annotation : paramAnnotations) {
                if (annotation instanceof ApiPathVariable) {
                    ApiPathVariable pathVariable = (ApiPathVariable)annotation;
                    String value = pathVariableMap.get(pathVariable.value());
                    if (value == null) {
                        throw new IllegalArgumentException("pathVariable,name=" + pathVariable.value() + " not not match,pathVariableMap=" + pathVariableMap);
                    }
                    return convert(apiMethodParam.getParamType(),value);
                }
            }
        }
        return null;
    }


    //解析patchVariable
    public void doPathVariableParse(PathMatcher matcher, ApiMethodMapping mapping, HttpContextRequest request){
        if(request.getAttachment(ApiMethodParamResolver.ATTACHMENT_API_METHOD_PATH_VARIABLE_KEY)==null) {
            Map<String, String> pathVariables = matcher.extractUriTemplateVariables(mapping.getUrlPattern(), request.getRequestUrl());
            request.addAttachment(ApiMethodParamResolver.ATTACHMENT_API_METHOD_PATH_VARIABLE_KEY, pathVariables);
        }
    }
}
