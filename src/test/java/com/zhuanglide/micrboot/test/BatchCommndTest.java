package com.zhuanglide.micrboot.test;

import com.zhuanglide.micrboot.http.HttpContextRequest;
import com.zhuanglide.micrboot.http.HttpContextResponse;
import com.zhuanglide.micrboot.mvc.ApiDispatcher;
import com.zhuanglide.micrboot.mvc.ModelAndView;
import com.zhuanglide.micrboot.mvc.annotation.ApiCommand;
import com.zhuanglide.micrboot.mvc.annotation.ApiMethod;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by wwj on 2017/4/1.
 */
@ApiCommand("batch")
public class BatchCommndTest {
    @Autowired(required = false)
    private ApiDispatcher apiDispatcher;

    @ApiMethod("/**")
    public ModelAndView batchRun(HttpContextRequest request, HttpContextResponse response){
        ModelAndView mv = new ModelAndView("jsonView");
        Map<String, Object> res = new HashMap<String, Object>();
        try {
            HttpContextRequest _req1 = request.clone();
            _req1.setRequestUrl("/t1");
            _req1.addParameter("name","auto name,xiaowang");
            _req1.addParameter("age","18");
            Object r1 = apiDispatcher.doProcess(_req1, response);
            HttpContextRequest _req2 = request.clone();
            _req2.setRequestUrl("/t2");
            Object r2 = apiDispatcher.doProcess0(_req2, response, null, false);
            if (r1 != null && r1 instanceof ModelAndView) {
                ModelAndView _mv = (ModelAndView) r1;
                r1 = _mv.getResult();
            }

            if (r2 != null && r2 instanceof ModelAndView) {
                ModelAndView _mv = (ModelAndView) r2;
                r2 = _mv.getResult();
            }
            res.put("t1", r1);
            res.put("t2", r2);
            mv.setResult(res);
        } catch (Exception te) {
            te.printStackTrace();
            mv.setResult("exception");
        }
        return mv;
    }
}
