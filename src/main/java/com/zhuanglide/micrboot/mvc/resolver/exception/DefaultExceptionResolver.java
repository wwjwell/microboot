package com.zhuanglide.micrboot.mvc.resolver.exception;

import com.zhuanglide.micrboot.mvc.ApiCommandMapping;
import com.zhuanglide.micrboot.http.HttpRequest;
import com.zhuanglide.micrboot.http.HttpResponse;
import com.zhuanglide.micrboot.mvc.resolver.ExceptionResolver;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;

/**
 * Created by wwj on 17/3/2.
 */
public class DefaultExceptionResolver implements ExceptionResolver {
    private Logger logger = LoggerFactory.getLogger(DefaultExceptionResolver.class);
    @Override
    public void resolveException(ApiCommandMapping mapping, HttpRequest request, HttpResponse response,Throwable ex) {
        logger.error("", ex);
        response.setStatus(HttpResponseStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
