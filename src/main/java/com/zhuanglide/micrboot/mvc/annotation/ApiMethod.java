package com.zhuanglide.micrboot.mvc.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiMethod {
    String value();
    enum HttpMethod {
        ALL("ALL"), GET("GET"), POST("POST"), UPDATE("UPDATE"), DELETE("DELETE")
        ,OPTIONS("OPTIONS"),HEAD("HEAD"),PUT("PUT"),PATCH("PATCH"),TRACE("TRACE"),CONNECT("CONNECT");
        private String methodType;
        HttpMethod(String methodType) {
            this.methodType = methodType;
        }
        public boolean equals(String method) {
            return method.equalsIgnoreCase(methodType);
        }
    }
    HttpMethod httpMethod() default HttpMethod.ALL;
}
