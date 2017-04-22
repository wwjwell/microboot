package com.zhuanglide.micrboot.mvc;


import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Map;

/**
 * ApiMapping包装
 */
public class ApiMethodMapping {

    private String url;

    private Object bean;

    private Object proxyTargetBean;

    private Method method;

    private String[] paramNames;

    private Type[] parameterTypes;

    private Map<Class,Object> extendFields;

    private Annotation[][] paramAnnotations;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
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

    public Map<Class, Object> getExtendFields() {
        return extendFields;
    }

    public void setExtendFields(Map<Class, Object> extendFields) {
        this.extendFields = extendFields;
    }

    public void setProxyTargetBean(Object proxyTargetBean) {
        this.proxyTargetBean = proxyTargetBean;
    }
}
