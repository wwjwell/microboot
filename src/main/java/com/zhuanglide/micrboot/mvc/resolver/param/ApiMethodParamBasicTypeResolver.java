package com.zhuanglide.micrboot.mvc.resolver.param;

import com.zhuanglide.micrboot.http.HttpContextRequest;
import com.zhuanglide.micrboot.http.HttpContextResponse;
import com.zhuanglide.micrboot.mvc.ApiMethodParam;
import com.zhuanglide.micrboot.mvc.annotation.ApiParam;
import com.zhuanglide.micrboot.mvc.resolver.ApiMethodParamResolver;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.math.BigInteger;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ApiMethodParamBasicTypeResolver implements ApiMethodParamResolver {
    private static final Set<Class> BASIC_TYPE_CLASS = new HashSet<Class>(){{add(String.class);
        add(BigInteger.class);
        add(Byte.class);
        add(byte.class);
        add(Short.class);
        add(short.class);
        add(Integer.class);
        add(int.class);
        add(Long.class);
        add(long.class);
        add(Float.class);
        add(float.class);
        add(Double.class);
        add(double.class);
        add(Character.class);
        add(char.class);
        add(Boolean.class);
        add(boolean.class);
        add(int[].class);
        add(long[].class);
        add(String[].class);
    }};
    private String arraySplit = ",";
    public boolean support(ApiMethodParam apiMethodParam) {
        return BASIC_TYPE_CLASS.contains(apiMethodParam.getParamType());
    }

    public Object getParamObject(ApiMethodParam apiMethodParam, HttpContextRequest request, HttpContextResponse response) throws Exception {
        if (support(apiMethodParam)) {
            Type type = apiMethodParam.getParamType();

            String paramName = apiMethodParam.getParamName();
            String paramDefaultValue = null;
            boolean required = true;

            Annotation[] paramAnnotations = apiMethodParam.getParamAnnotations();
            ApiParam apiParamAnnotation = null;

            if (paramAnnotations != null) {
                for (Annotation paramAnnotation : paramAnnotations) {
                    if (paramAnnotation instanceof ApiParam) {
                        apiParamAnnotation = (ApiParam) paramAnnotation;
                        paramName = apiParamAnnotation.value();
                        paramDefaultValue = apiParamAnnotation.defaultValue();
                        required = apiParamAnnotation.required();
                        break;
                    }
                }
            }

            String paramValue = request.getParameter(paramName);
            if (required && paramValue == null) {
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

            if (type.equals(String.class)) {
                return paramValue;
            }else if(type.equals(BigInteger.class)){
                return null!=paramValue?new BigInteger(paramValue):null;
            } else if (type.equals(Integer.class) || type.equals(int.class)) {
                return Integer.parseInt(paramValue);
            } else if (type.equals(Long.class) || type.equals(long.class)) {
                return Long.parseLong(paramValue);
            } else if (type.equals(Float.class) || type.equals(float.class)) {
                return Float.parseFloat(paramValue);
            } else if (type.equals(Double.class) || type.equals(double.class)) {
                return Double.parseDouble(paramValue);
            } else if (type.equals(Character.class) || type.equals(char.class)) {
                if (null != paramValue) {
                    return paramValue.charAt(0);
                }
            } else if (type.equals(Byte.class) || type.equals(byte.class)) {
                return (byte) Integer.parseInt(paramValue);
            } else if (type.equals(Short.class) || type.equals(short.class)) {
                return (short) Integer.parseInt(paramValue);
            } else if (type.equals(Boolean.class) || type.equals(boolean.class)) {
                return Boolean.parseBoolean(paramValue);
            } else if (type.equals(int[].class) || type.equals(int[].class)) {
                String[] values = paramValue.split(arraySplit);
                int[] array = new int[values.length];
                for (int i = 0; i < values.length; i++) {
                    array[i] = Integer.parseInt(values[i]);
                }
                return array;
            } else if (type.equals(long[].class) || type.equals(long[].class)) {
                String[] values = paramValue.split(arraySplit);
                long[] array = new long[values.length];
                for (int i = 0; i < values.length; i++) {
                    array[i] = Long.parseLong(values[i]);
                }
                return array;
            } else if (type.equals(String[].class) || type.equals(String[].class)) {
                return paramValue.split(arraySplit);
            }else{
                return null;
            }
        }
        return null;
    }

    public void setArraySplit(String arraySplit) {
        this.arraySplit = arraySplit;
    }
}
