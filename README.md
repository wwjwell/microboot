# micrboot
#### 是什么，干什么
  基于netty高性能网络框架的HTTP RESTful服务器，非常适合后台接口的微服务。
  和作者一样不喜欢微服务还要依赖一个重量级的WEB容器的同学,绝对是不二的选择
#### micrboot架构图
- ![image](https://github.com/wwjwell/micrboot/raw/master/docs/micrboot.png)
#### 优点
- 设计思想基于springMVC，用法也是springMVC的简化版（只不过注解不一样，也更简陋，不过对于后端接口服务，基本能满足所有的需要）
- 使用简单，上手快，只要学过springMVC 基本没有任何学习瓶颈
- 多接口合并
- netty网络框架作为服务，剔除了web容器，更轻量，部署更简单
- 性能优越，默认配置不属于各种调优后的tomcat性能，现在环境都是虚拟机，tomcat配置你确定你能调优好？
- 没有j2ee规范，就是最直接RESTful架构的http服务器

#### 缺点
- 没有使用j2ee的规范，所以view层是无法对jsp渲染，现在也只做了JSON 和String 两种，后续会加上freemark 和静态资源，
- 不是标准，非权威，一般不容易被接受（ 唉！）

### 为什么写micrboot 
####   Why？
- 1、后端的纯接口，只是http的request 和response 交互，不涉及jsp等模板，j2ee的规范在这方面不简洁
- 2、微服务越来越多的，springBoot优势越来越大，但是原则上还是没有脱离web容器，而一个简单的http server 可能更是一种换汤换药的方式
- 3、多接口的合并。（例：APP跟后端服务的http调用，这样可能省时、省流量。）
- 4、配置简陋的虚拟机上表现良好
- 5、流量异常暴增，平稳支撑，系统负载也不会像tomcat那样嗷嗷飙升
#### 表现
  目前micrboot正在做性能测试,将对比tomcat,只做Http解码的Netty服务,性能测试与tomcat相比占用系统资源更少
  micrboot已经运用于生产环境，运行平稳，我直接放上micrboot的线上表现
* 两台docker虚拟机，配置是2核8G内存，STAT硬盘400G。

| 指标     | 单位      | 监控                                       |
| :----- | :------ | :--------------------------------------- |
| 流量     | TPS/min | ![image](https://github.com/wwjwell/micrboot/raw/master/docs/qps_min.png =100x100) |
| 平均耗时   | ms      | ![image](https://github.com/wwjwell/micrboot/raw/master/docs/cost.png =100x100) |
| 内存使用率  | 8G-%    | ![image](https://github.com/wwjwell/micrboot/raw/master/docs/cost.png =100x100) |
| CPU使用率 | 100%    | ![image](https://github.com/wwjwell/micrboot/raw/master/docs/cpu_use.png =100x100) |


## 怎么启动
* maven配置
```
    <dependency>
      <groupId>com.zhuanglide</groupId>
      <artifactId>micrboot</artifactId>
      <version>1.0.1</version>
    </dependency>

```
* 设置spring配置文件

具体可以参照micrboot-demo/src/main/resources/api.xml
```
    <context:component-scan base-package="com.zhuanglide.micrboot.demo.**">
        <!-- 扫描ApiCommand注解 -->
        <context:include-filter type="annotation" expression="com.zhuanglide.micrboot.mvc.annotation.ApiCommand"/>
    </context:component-scan>
    <bean name="server" class="com.zhuanglide.micrboot.ServerConfig">
        <property name="port" value="8080"/> <!-- set port=8080 -->
    </bean>    
    <!-- config server -->
    <bean name="server" class="com.zhuanglide.micrboot.Server">
        <property name="serverConfig" ref="serverConfig"/> <!-- set port=8080 -->
    </bean>
```
* 增加业务接口 
  micrboot-demo/src/main/java/com.zhuanglide.micrboot.demo.command.ApiCommandTest
```
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

        //restful ,只针对GET请求
        @ApiMethod("/t2",httpMethod = ApiMethod.HttpMethod.GET)
        public ModelAndView test2(){
            ModelAndView mv = new ModelAndView("JSON_VIEW");
            Map<String, Object> res = new HashMap<String, Object>();
            res.put("Hello","这是一");
            res.put("gaga", new Date());
            mv.setResult(res);
            return mv;
        }

         @ApiMethod("/detail/{id}")
            public ModelAndView detail(@ApiPathVariable("id")int id,@ApiParam("name")String name){
                ModelAndView mv = new ModelAndView("jsonView");
                Map<String, Object> res = new HashMap<String, Object>();
                res.put("Hello","这是一");
                res.put("gaga", new Date());
                res.put("detailId", id);
                res.put("name", name);
                mv.setResult(res);
                return mv;
            }
    }
```

* 怎么调用
  如上例子：http://localhost:8080/t1?name=哈哈&age=123

 返回：
 ```
{
    "b": 1491034714263,
    "age": 123,
    "name": "哈哈",
    "a": "我的家"
}
 ```
具体可以参照micrboot-demo/src/main/java/com.zhuanglide.micrboot.demo.Main
```
    public static void main(String[] args) {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("classpath:api.xml");
        Server server = context.getBean(Server.class);
        server.start();
    }
```


## 拦截器
* 类似spring MVC的拦截器
```
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
```
增加拦截器，需要在spring配置文件中显示调用
```
    <bean class="com.zhuanglide.micrboot.demo.interceptor.TestInterceptor">
        <property name="order" value="1"/>
    </bean>
```

## 多接口合并

* 设置spring配置文件 需要增加一个设置

参考micrboot-demo/src/main/resources/api.xml
```
    <bean class="com.zhuanglide.micrboot.mvc.ApiDispatcher"/>
```

简单示例，在实际生产中，可以根据约定，自动解析要执行的接口，如果对执行顺序无要求，最好使用多线程的方式去执行，如果有严格要求，需要顺序的执行，使用者可以灵活掌握。
```
@ApiCommand("batch")
public class BatchCommndTest {
    @Autowired(required = false)
    private ApiDispatcher apiDispatcher;

    @ApiMethod("run")
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
            Object r2 = apiDispatcher.doProcess(_req2, response, null,false);
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
* 上述batch/run返回结果 http://localhost:8080/batch/run
```
{
  "t2": {
    "gaga": 1491035540912,
    "Hello": "这是一"
  },
  "t1": {
    "b": 1491035539497,
    "age": 18,
    "name": "auto name,xiaowang",
    "a": "我的家"
  }
}
```

## 自定义参数注入
* 自定义要注入的参数,例
```
public class CustomerParam {
    public String customer;
}
```
* 创建ApiMethodParamResolver来处理参数注入
```
public class CustomerApiMethodParamResolver extends 	AbstractApiMethodParamResolver {
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
```
* spring声明改处理类
```
<bean class="com.zhuanglide.micrboot.demo.resolver.CustomerApiMethodParamResolver"/>
```
* 使用
```
@ApiMethod("cus")
    public String cus(CustomerParam param) {
        return param.customer;
    }
```
