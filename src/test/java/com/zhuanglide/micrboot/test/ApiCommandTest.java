package com.zhuanglide.micrboot.test;

import com.zhuanglide.micrboot.mvc.ModelAndView;
import com.zhuanglide.micrboot.mvc.annotation.ApiCommand;
import com.zhuanglide.micrboot.mvc.annotation.ApiMethod;
import com.zhuanglide.micrboot.mvc.annotation.ApiParam;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by wwj on 17/3/9.
 */
@ApiCommand
public class ApiCommandTest{
    @ApiMethod("/t1")
    public ModelAndView test1(@ApiParam("name")String name,@ApiParam("age")int age){

        ModelAndView mv = new ModelAndView("jsonView");
        Map<String, Object> res = new HashMap<String, Object>();
        res.put("a","我的家");
        res.put("b", new Date());
        res.put("name", name);
        res.put("age", age);
        mv.setResult(res);
        return mv;
    }

    @ApiMethod("/t2")
    public ModelAndView test2(){
        ModelAndView mv = new ModelAndView("jsonView");
        Map<String, Object> res = new HashMap<String, Object>();
        res.put("Hello","这是一");
        res.put("gaga", new Date());
        mv.setResult(res);
        return mv;
    }
}
