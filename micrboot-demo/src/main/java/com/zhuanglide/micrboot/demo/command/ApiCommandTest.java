package com.zhuanglide.micrboot.demo.command;

import com.zhuanglide.micrboot.demo.resolver.CustomerParam;
import com.zhuanglide.micrboot.http.HttpContextRequest;
import com.zhuanglide.micrboot.http.HttpContextResponse;
import com.zhuanglide.micrboot.mvc.ModelAndView;
import com.zhuanglide.micrboot.mvc.annotation.*;
import io.netty.handler.codec.http.multipart.FileUpload;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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

    @ApiMethod(value = "/t2",httpMethod = ApiMethod.HttpMethod.GET)
    public ModelAndView test2(){
        ModelAndView mv = new ModelAndView("jsonView");
        Map<String, Object> res = new HashMap<String, Object>();
        res.put("Hello","这是一");
        res.put("gaga", new Date());
        mv.setResult(res);
        return mv;
    }

    @ApiMethod("/t3")
    public String t3() {
        return "狮驼岭";
    }

    @ApiMethod("/detail/{id}")
    public ModelAndView detail(@ApiPathVariable("id")int id,String abc,@ApiParam("name")String name){
        ModelAndView mv = new ModelAndView("jsonView");
        Map<String, Object> res = new HashMap<String, Object>();
        res.put("Hello","这是一");
        res.put("gaga", new Date());
        res.put("detailId", id);
        res.put("name", name);
        mv.setResult(res);
        return mv;
    }

    @ApiMethod("/body/{id}")
    public String body(@ApiPathVariable("id")int id,@ApiRequestBody String body){
        return body + "\n" + id;
    }

    @ApiMethod("cus")
    public String cus(CustomerParam param) {
        return param.customer;
    }


    @ApiMethod("download")
    public void download(HttpContextRequest request, HttpContextResponse response){
        try {
            File file = new File("/Users/wwj/test.png");
            FileInputStream fileInputStream = new FileInputStream(file);
            response.setFile(file);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }
}
