package com.github.wwjwell.microboot.mvc;


import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

/**
 * ApiMapping包装
 */
public class ApiMethodMapping {

    private String urlPattern;

    private Object bean;

    private Object proxyTargetBean;

    private Method method;

    private String[] paramNames;

    private Type[] parameterTypes;

    private Annotation[][] paramAnnotations;

    public String getUrlPattern() {
        return urlPattern;
    }

    public void setUrlPattern(String urlPattern) {
        this.urlPattern = urlPattern;
    }

    public void setBean(Object bean) {
        this.bean = bean;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public Object getBean() {
        return bean;
    }

    public String[] getParamNames() {
        return paramNames;
    }

    public void setParamNames(String[] paramNames) {
        this.paramNames = paramNames;
    }

    public Type[] getParameterTypes() {
        return parameterTypes;
    }

    public void setParameterTypes(Type[] parameterTypes) {
        this.parameterTypes = parameterTypes;
    }

    public Annotation[][] getParamAnnotations() {
        return paramAnnotations;
    }

    public void setParamAnnotations(Annotation[][] paramAnnotations) {
        this.paramAnnotations = paramAnnotations;
    }

    public Object getProxyTargetBean() {
        return proxyTargetBean;
    }

    public void setProxyTargetBean(Object proxyTargetBean) {
        this.proxyTargetBean = proxyTargetBean;
    }

}
