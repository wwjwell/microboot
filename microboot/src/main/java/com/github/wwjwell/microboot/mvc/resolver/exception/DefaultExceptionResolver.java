package com.github.wwjwell.microboot.mvc.resolver.exception;

import com.github.wwjwell.microboot.constants.Constants;
import com.github.wwjwell.microboot.http.HttpContextRequest;
import com.github.wwjwell.microboot.http.HttpContextResponse;
import com.github.wwjwell.microboot.mvc.ApiMethodMapping;
import com.github.wwjwell.microboot.mvc.resolver.ExceptionResolver;
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
    public void resolveException(ApiMethodMapping mapping, HttpContextRequest request, HttpContextResponse response, Throwable ex) {
        if((ex instanceof IllegalArgumentException) && ex.getMessage()!=null){
            response.setStatus(HttpResponseStatus.BAD_REQUEST);
            logger.warn("reqId="+request.getAttachment(Constants.REQ_ID), ex);
        }else{
            response.setStatus(HttpResponseStatus.INTERNAL_SERVER_ERROR);
            logger.error("reqId="+request.getAttachment(Constants.REQ_ID), ex);
        }
        try {
            String msg = ex.getMessage();
            if (null == msg) {
                msg = ex.toString();
            }
            response.setContent(msg);
        } catch (Exception e) {
            response.setContent(String.valueOf(e));
        }
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
