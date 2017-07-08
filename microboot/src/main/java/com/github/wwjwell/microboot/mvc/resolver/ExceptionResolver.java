package com.github.wwjwell.microboot.mvc.resolver;

import com.github.wwjwell.microboot.http.HttpContextRequest;
import com.github.wwjwell.microboot.http.HttpContextResponse;
import com.github.wwjwell.microboot.mvc.ApiMethodMapping;
import org.springframework.core.Ordered;

/**
 * Created by wwj on 17/3/2.
 */
public interface ExceptionResolver extends Ordered{
    void resolveException(ApiMethodMapping mapping, HttpContextRequest request, HttpContextResponse response, Throwable ex);
}
