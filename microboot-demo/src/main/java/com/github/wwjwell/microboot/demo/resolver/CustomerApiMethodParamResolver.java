package com.github.wwjwell.microboot.demo.resolver;

import com.github.wwjwell.microboot.http.HttpContextRequest;
import com.github.wwjwell.microboot.http.HttpContextResponse;
import com.github.wwjwell.microboot.mvc.ApiMethodParam;
import com.github.wwjwell.microboot.mvc.resolver.param.AbstractApiMethodParamResolver;

/**
 * Created by wwj on 2017/6/6.
 */
public class CustomerApiMethodParamResolver extends AbstractApiMethodParamResolver {
    @Override
    public boolean support(ApiMethodParam apiMethodParam) {
        return apiMethodParam.getParamType().equals(CustomerParam.class);
    }

    @Override
    public Object getParamObject(ApiMethodParam apiMethodParam, HttpContextRequest request, HttpContextResponse response) throws Exception {
        CustomerParam param = new CustomerParam();
        param.customer = "哈哈，customer";
        return param;
    }
}
