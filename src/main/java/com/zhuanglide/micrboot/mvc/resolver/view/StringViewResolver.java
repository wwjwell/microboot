package com.zhuanglide.micrboot.mvc.resolver.view;

import com.zhuanglide.micrboot.http.HttpContextRequest;
import com.zhuanglide.micrboot.http.HttpContextResponse;
import com.zhuanglide.micrboot.mvc.ModelAndView;
import com.zhuanglide.micrboot.mvc.resolver.ViewResolver;

/**
 * Created by wwj on 17/3/22.
 */
public class StringViewResolver extends ViewResolver {
    private String contentType = "text/html";
    @Override
    public ModelAndView resolve(Object result) {
        if (result instanceof ModelAndView) {
            ModelAndView mv = (ModelAndView)result;
            if (mv.getResult() instanceof String) {
                return mv;
            }else{
                return null;
            }
        }else if(result instanceof String){
            ModelAndView mv = new ModelAndView();
            mv.setResult(result);
            return mv;
        }
        return null;
    }

    @Override
    public void render(ModelAndView mv, HttpContextRequest request, HttpContextResponse response) throws Exception {
        response.setContent((String) mv.getResult());
    }
}
