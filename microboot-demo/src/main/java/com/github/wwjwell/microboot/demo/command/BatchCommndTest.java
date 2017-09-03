package com.github.wwjwell.microboot.demo.command;

import com.github.wwjwell.microboot.http.HttpContextRequest;
import com.github.wwjwell.microboot.http.HttpContextResponse;
import com.github.wwjwell.microboot.mvc.ApiDispatcher;
import com.github.wwjwell.microboot.mvc.ModelAndView;
import com.github.wwjwell.microboot.mvc.annotation.ApiMethod;
import com.github.wwjwell.microboot.mvc.annotation.ApiCommand;
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
             /**根据参数method来决定调用哪些接口 */
//            String method = request.getParameter("method");
//            String[] urls = null;
//            if(null != method){
//                urls = method.split(",");
//            }
//            for (String url : urls) {
//                HttpContextRequest req = request.clone();
//                req.setRequestUrl(url);
//                //div setting
//            }

            /** 为了例子简单，直接写死为/t1,/t2 */
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
            //package response
            /** 实际生产环境根据约定来处理，对于复杂的httpResponse，无法支持，比如文件上传下载，http response status != 200情况*/
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
