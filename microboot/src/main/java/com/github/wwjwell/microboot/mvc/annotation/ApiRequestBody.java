package com.zhuanglide.microboot.mvc.annotation;

import java.lang.annotation.*;

/**
 * Created by wwj on 2017/5/27.
 */

@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ApiRequestBody {
}
