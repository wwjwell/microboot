package com.zhuanglide.micrboot.mvc.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiMethod {
    String value();
    enum RequestMethod {
        ALL("ALL"), GET("GET"), POST("POST"), UPDATE("UPDATE"), DELETE("DELETE");
        private String methodType;
        RequestMethod(String methodType) {
            this.methodType = methodType;
        }
        public boolean equals(String method) {
            return method.equalsIgnoreCase(methodType);
        }
    }
    RequestMethod method() default RequestMethod.ALL;
}
