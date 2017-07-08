package com.github.wwjwell.microboot.mvc.resolver.param;

import com.github.wwjwell.microboot.http.HttpContextRequest;
import com.github.wwjwell.microboot.http.HttpContextResponse;
import com.github.wwjwell.microboot.mvc.annotation.ApiParam;
import com.github.wwjwell.microboot.mvc.ApiMethodParam;
import com.github.wwjwell.microboot.mvc.annotation.ApiRequestBody;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.math.BigInteger;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ApiMethodParamBasicTypeResolver extends AbstractApiMethodParamResolver {
    private static final Set<Class> BASIC_TYPE_CLASS = new HashSet<Class>(){{
        add(String.class);
        add(Integer.class); add(int.class);
        add(Long.class); add(long.class);
        add(Float.class); add(float.class);
        add(Double.class); add(double.class);
        add(Character.class); add(char.class);
        add(Byte.class); add(byte.class);
        add(Short.class); add(short.class);
        add(Boolean.class); add(boolean.class);
        add(BigInteger.class);
        add(int[].class);add(Integer[].class);
        add(long[].class);add(Long[].class);
        add(String[].class);
    }};

    public boolean support(ApiMethodParam apiMethodParam) {
        Annotation[] paramAnnotations = apiMethodParam.getParamAnnotations();
        if (null != paramAnnotations) {
            for (Annotation annotation : paramAnnotations) {
                if ((annotation instanceof ApiParam || annotation instanceof ApiRequestBody)
                        && BASIC_TYPE_CLASS.contains(apiMethodParam.getParamType())) {
                    return true;
                }
            }
        }
        return false;
    }

    public Object getParamObject(ApiMethodParam apiMethodParam, HttpContextRequest request, HttpContextResponse response) throws Exception {
        if (support(apiMethodParam)) {
            Type type = apiMethodParam.getParamType();
            Annotation[] paramAnnotations = apiMethodParam.getParamAnnotations();
            String paramValue = null;
            String paramName = apiMethodParam.getParamName();
            if (paramAnnotations != null) {
                for (Annotation paramAnnotation : paramAnnotations) {
                    if (paramAnnotation instanceof ApiParam) {
                        ApiParam apiParamAnnotation = (ApiParam) paramAnnotation;
                        paramName = apiParamAnnotation.value();
                        String paramDefaultValue = apiParamAnnotation.defaultValue();
                        paramValue = request.getParameter(paramName);
                        if (apiParamAnnotation.required() && paramValue == null) {
                            throw new IllegalArgumentException("param=" + paramName +" is required");
                        }
                        if (paramValue == null) {
                            paramValue = paramDefaultValue;
                        }
                        if (apiParamAnnotation != null && apiParamAnnotation.validateRegx().length()>0) {
                            Pattern pattern = Pattern.compile(apiParamAnnotation.validateRegx());
                            Matcher matcher = pattern.matcher(String.valueOf(paramValue));
                            if (!matcher.find()) {
                                throw new IllegalArgumentException("param=" + paramName +",value="+paramValue+" is illegal,pattern="+apiParamAnnotation.validateRegx());
                            }
                        }
                        break;
                    }else if(paramAnnotation instanceof ApiRequestBody){
                        paramValue = request.getBody();
                        break;
                    }
                }
            }
            try {
                return convert(type, paramValue);
            } catch (Exception e) {
                throw new IllegalArgumentException("convert param failed,paramName=" + paramName + ",type=" + type + ",value=" + paramValue);
            }
        }
        return null;
    }
}
