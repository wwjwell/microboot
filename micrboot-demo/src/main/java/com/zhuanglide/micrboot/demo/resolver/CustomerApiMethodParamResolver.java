package com.zhuanglide.micrboot.demo.resolver;

import com.zhuanglide.micrboot.http.HttpContextRequest;
import com.zhuanglide.micrboot.http.HttpContextResponse;
import com.zhuanglide.micrboot.mvc.ApiMethodParam;
import com.zhuanglide.micrboot.mvc.resolver.param.AbstractApiMethodParamResolver;

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
