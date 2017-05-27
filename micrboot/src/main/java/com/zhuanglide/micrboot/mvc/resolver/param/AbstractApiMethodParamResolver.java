package com.zhuanglide.micrboot.mvc.resolver.param;

import com.zhuanglide.micrboot.http.HttpContextRequest;
import com.zhuanglide.micrboot.http.HttpContextResponse;
import com.zhuanglide.micrboot.mvc.ApiMethodMapping;
import com.zhuanglide.micrboot.mvc.ApiMethodParam;
import com.zhuanglide.micrboot.mvc.resolver.ApiMethodParamResolver;

import java.lang.reflect.Type;
import java.math.BigInteger;

/**
 * Created by wwj on 2017/4/23.
 */
public abstract class AbstractApiMethodParamResolver implements ApiMethodParamResolver {
    protected int order = Integer.MAX_VALUE;
    protected String arraySplit = ",";
    public abstract boolean support(ApiMethodParam apiMethodParam) ;

    public abstract Object getParamObject(ApiMethodParam apiMethodParam, HttpContextRequest request, HttpContextResponse response) throws Exception;


    protected Object convert(Type type, String value) {
        if (value == null) {
            return null;
        }
        if (type.equals(String.class)) {
            return value;
        }  else if (type.equals(Integer.class) || type.equals(int.class)) {
            return Integer.parseInt(value);
        } else if (type.equals(Long.class) || type.equals(long.class)) {
            return Long.parseLong(value);
        } else if (type.equals(Float.class) || type.equals(float.class)) {
            return Float.parseFloat(value);
        } else if (type.equals(Double.class) || type.equals(double.class)) {
            return Double.parseDouble(value);
        } else if (type.equals(Character.class) || type.equals(char.class)) {
            if (null != value) {
                return value.charAt(0);
            }
        } else if (type.equals(Byte.class) || type.equals(byte.class)) {
            return (byte) Integer.parseInt(value);
        } else if (type.equals(Short.class) || type.equals(short.class)) {
            return (short) Integer.parseInt(value);
        } else if (type.equals(Boolean.class) || type.equals(boolean.class)) {
            return Boolean.parseBoolean(value);
        } else if (type.equals(BigInteger.class)) {
            return null != value ? new BigInteger(value) : null;
        } else if (type.equals(int[].class) || type.equals(int[].class)) {
            String[] values = value.split(arraySplit);
            int[] array = new int[values.length];
            for (int i = 0; i < values.length; i++) {
                array[i] = Integer.parseInt(values[i]);
            }
            return array;
        } else if (type.equals(long[].class) || type.equals(long[].class)) {
            String[] values = value.split(arraySplit);
            long[] array = new long[values.length];
            for (int i = 0; i < values.length; i++) {
                array[i] = Long.parseLong(values[i]);
            }
            return array;
        } else if (type.equals(String[].class) || type.equals(String[].class)) {
            return value.split(arraySplit);
        }
        return null;
    }

    @Override
    public void prepare(ApiMethodMapping mapping, HttpContextRequest request, Object... args) {
    }

    public void setArraySplit(String arraySplit) {
        this.arraySplit = arraySplit;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    @Override
    public int getOrder() {
        return order;
    }
}
