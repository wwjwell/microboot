package com.zhuanglide.micrboot.mvc;

/**
 * Created by wwj on 17/3/9.
 */
public class ModelAndView {
    //viewName
    protected String viewName;
    protected Object result;
    public ModelAndView(){}

    public ModelAndView(String viewName){
        this.viewName = viewName;
    }
    public String getViewName(){
        return this.viewName;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }
}
