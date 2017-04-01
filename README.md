#类springMVC的轻量级HTTP服务框架，使用NETTY作为服务端，适用于后端纯接口服务器，尤其是APP的服务端，支持接口合并功能

##特点
* 类springMVC架构
* netty网络框架作为服务，剔除了web容器
* 支持拦截器、多接口合并、目前支持了json view 和纯string view，支持文件上传下载
    
## 怎么启动
* 设置spring配置文件

具体可以参照test/resource/api.xml
````xml
    <context:component-scan base-package="com.zhuanglide.micrboot.test.**">
        <!-- 扫描ApiCommand注解 -->
        <context:include-filter type="annotation" expression="com.zhuanglide.micrboot.mvc.annotation.ApiCommand"/>
    </context:component-scan>
        
    <!-- config server -->
    <bean name="server" class="com.zhuanglide.micrboot.Server">
        <property name="port" value="8080"/> <!-- set port=8080 -->
    </bean>
````
* 增加业务接口 
可以参考test/java/com.zhuanglide.micrboot.test.ApiCommandTest
````java
    @ApiCommand
    public class ApiCommandTest{
        @ApiMethod("/t1")
        public ModelAndView test1(@ApiParam("name")String name,@ApiParam("age")int age){
            ModelAndView mv = new ModelAndView("JSON_VIEW");
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
            ModelAndView mv = new ModelAndView("JSON_VIEW");
            Map<String, Object> res = new HashMap<String, Object>();
            res.put("Hello","这是一");
            res.put("gaga", new Date());
            mv.setResult(res);
            return mv;
        }
    }
````

* 怎么调用
如上例子：http://localhost:8080/t1?name=哈哈&age=123
  
 返回：
 ```json
{
    "b": 1491034714263,
    "age": 123,
    "name": "哈哈",
    "a": "我的家"
}
```
具体可以参照test/java/com.zhuanglide.micrboot.test.TestServer
````java
    @RunWith(SpringJUnit4ClassRunner.class)
    @ContextConfiguration(locations = {"classpath*:api.xml"})
    public class TestServer {
        @Autowired
        private Server server;
        @Test
        public void main(){
            server.start();
        }
    }
````


## 拦截器
* 类似spring MVC的拦截器
````java
    public class TestInterceptor extends AbstractApiInterceptor {
        int order ; //拦截器顺序
        //分发之前，甚至可以更改最终invoke的接口
        @Override
        public boolean preDispatch(HttpRequest request, HttpResponse response) {
            System.out.println("TestInterceptor preDispatch url="+request.getRequestUrl());
            return super.preDispatch(request, response);
        }
        
        //执行invoke方法之前，可以更改，增加删除参数值
        @Override
        public void postHandler(ApiCommandMapping mapping, HttpRequest request, HttpResponse response) {
            System.out.println("TestInterceptor postHandler url="+request.getRequestUrl());
            super.postHandler(mapping, request, response);
        }
        
        //执行invoke方法之后，可以修改返回值
        @Override
        public void afterHandle(ApiCommandMapping mapping, Object modelView, HttpRequest request, HttpResponse response, Throwable throwable) {
            System.out.println("TestInterceptor afterHandle url="+request.getRequestUrl());
            super.afterHandle(mapping, modelView, request, response, throwable);
        }
    }
````
增加拦截器，需要在spring配置文件中显示调用
````xml
    <bean class="com.zhuanglide.micrboot.test.TestInterceptor">
        <property name="order" value="1"/>
    </bean>
````

## 多接口合并

* 设置spring配置文件 需要增加一个设置

参考test/resource/api_batch.xml
```xml
    <bean class="com.zhuanglide.micrboot.mvc.ApiDispatcher"/>
```

简单示例，在实际生产中，可以根据约定，自动解析要执行的接口，如果对执行顺序无要求，最好使用多线程的方式去执行，如果有严格要求，需要顺序的执行，使用者可以灵活掌握。
```java
@ApiCommand("batch")
public class BatchCommndTest {
    @Autowired(required = false)
    private ApiDispatcher apiDispatcher;

    @ApiMethod("run")
    public ModelAndView batchRun(HttpRequest request, HttpResponse response){
        ModelAndView mv = new ModelAndView("jsonView");
        Map<String, Object> res = new HashMap<String, Object>();
        try {
            HttpRequest _req1 = request.clone();
            _req1.setRequestUrl("/t1");
            _req1.addParameter("name","auto name,xiaowang");
            _req1.addParameter("age","18");
            Object r1 = apiDispatcher.innerDoService(_req1, response);
            HttpRequest _req2 = request.clone();
            _req2.setRequestUrl("/t2");
            Object r2 = apiDispatcher.innerDoService(_req2, response, null,false);
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
```