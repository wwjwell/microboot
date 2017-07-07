package com.zhuanglide.microboot.mvc.resolver;

import com.zhuanglide.microboot.mvc.ApiMethodMapping;
import com.zhuanglide.microboot.http.HttpContextRequest;
import com.zhuanglide.microboot.http.HttpContextResponse;
import org.springframework.core.Ordered;

/**
 * Created by wwj on 17/3/2.
 */
public interface ExceptionResolver extends Ordered{
    void resolveException(ApiMethodMapping mapping, HttpContextRequest request, HttpContextResponse response, Throwable ex);
}
