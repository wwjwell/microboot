package com.zhuanglide.micrboot.mvc.resolver;

import com.zhuanglide.micrboot.mvc.ApiCommandMapping;
import com.zhuanglide.micrboot.http.HttpRequest;
import com.zhuanglide.micrboot.http.HttpResponse;
import org.springframework.core.Ordered;

/**
 * Created by wwj on 17/3/2.
 */
public interface ExceptionResolver extends Ordered{
    void resolveException(ApiCommandMapping mapping, HttpRequest request, HttpResponse response, Throwable ex);
}
