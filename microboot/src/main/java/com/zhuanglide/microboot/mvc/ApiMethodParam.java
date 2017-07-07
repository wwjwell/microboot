package com.zhuanglide.microboot.mvc;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

/**
 * 封装ApiMethod注解的信息
 */
public class ApiMethodParam {

    private Method method;

    private String paramName;

    private Annotation[] paramAnnotations;

    private Type paramType;

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public String getParamName() {
        return paramName;
    }

    public void setParamName(String paramName) {
        this.paramName = paramName;
    }

    public Annotation[] getParamAnnotations() {
        return paramAnnotations;
    }

    public void setParamAnnotations(Annotation[] paramAnnotations) {
        this.paramAnnotations = paramAnnotations;
    }

    public Type getParamType() {
        return paramType;
    }

    public void setParamType(Type paramType) {
        this.paramType = paramType;
    }
}
