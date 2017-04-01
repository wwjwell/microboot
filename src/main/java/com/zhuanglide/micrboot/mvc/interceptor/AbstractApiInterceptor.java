package com.zhuanglide.micrboot.mvc.interceptor;

import com.zhuanglide.micrboot.mvc.ApiCommandMapping;
import com.zhuanglide.micrboot.http.HttpRequest;
import com.zhuanglide.micrboot.http.HttpResponse;
import org.springframework.core.Ordered;

/**
 * Created by wwj on 16/10/20.
 */
public abstract class AbstractApiInterceptor implements ApiInterceptor,Ordered {
    protected int order = Ordered.LOWEST_PRECEDENCE + 100;
    @Override
    public boolean preDispatch(HttpRequest request, HttpResponse response) {
        return true;
    }

    @Override
    public void postHandler(ApiCommandMapping mapping, HttpRequest request, HttpResponse response) {
    }

    @Override
    public void afterHandle(ApiCommandMapping mapping, Object modelView, HttpRequest request, HttpResponse response, Throwable throwable) {

    }

    public void setOrder(int order) {
        this.order = order;
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
