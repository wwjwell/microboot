package com.zhuanglide.micrboot.mvc.resolver;

import com.zhuanglide.micrboot.mvc.ApiMethodMapping;
import com.zhuanglide.micrboot.http.HttpContextRequest;
import com.zhuanglide.micrboot.http.HttpContextResponse;
import org.springframework.core.Ordered;

/**
 * Created by wwj on 17/3/2.
 */
public interface ExceptionResolver extends Ordered{
    void resolveException(ApiMethodMapping mapping, HttpContextRequest request, HttpContextResponse response, Throwable ex);
}
