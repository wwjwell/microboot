package com.github.wwjwell.microboot.mvc.annotation;

import java.lang.annotation.*;

/**
 * Created by wwj on 2017/4/23.
 */
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ApiPathVariable {
    String value() default "";
}
