# micrboot 是什么，能做什么？
### micrboot是什么
micrboot 是基于Netty 开发的一个Http服务框架，自身提供http server功能，jar包方式启动，使用方式与架构与SpringMVC极其相似，micrboot没有遵循j2ee规范，非常轻量级，并且高性能。
### micrboot能做什么
micrboot天生是为了后端纯接口服务做得框架，具有开发、部署简单，高性能并且稳定，支持所有http请求方式，支持多视图，并且可以根据自己的要求自定义ViewResolver返回自己想要的结果，目前服务只String、JSONView 两种返回。

# micrboot 优缺点
### 优点
- 超级轻量级的框架
- 性能优越，稳定，尤其对于非常暴增的流量，能够稳定支撑
- 使用简单，上手快，只要学过springMVC 基本没有任何学习瓶颈
- 非常干净，不占用多余端口，非特殊情况不需要对他的性能调优
- 开发简单、启动快，jar包启动
- 支持一机多应用，节省开发、测试服务器资源
- 许多后端接口使用的特性支持，如多接口合并
- 等等
### 缺点
- 由于不支持j2ee规范，所以不能支持jsp（绝对不会支持），目前还不支持静态资源(作者还没写，以后会支持)，freemark等模板语言不支持(作者还没有写，可以自己处理直接返回String)
- 不是标准，非权威

# micrboot设计思想
micrboot设计思想源于springMVC，摒弃J2EE繁杂的规范，更加纯粹的进行HTTP编程，不依赖于任何第三方容器，运用高性能的Netty框架做server ，以及netty的线程模型来处理业务，后续作者想拓展为更为轻量级的Actor模式的并发模型，使性能更加强劲。一旦你使用了micrboot，或许你再也不想用tomcat做你的容器了。
- ![image](https://github.com/wwjwell/micrboot/raw/master/docs/micrboot.png)

# micrboot依赖情况
micrboot强依赖于netty 、jackson、slf4j、spring，需要你在项目中引入这4个jar

# micrboot性能如何

作者做过几次性能测试，每次和每次都不是很一样，搞得我很郁闷，可能与我测试方式和我所拥有的资源有关。不过整体表现在性能上不输于tomcat，尤其在相同流量上，系统负载能比tomcat低2到3倍。
无图无真相，作者贴上micrboot生产环境的表现，当然这不是micrboot最优的表现，线上还有过比这个指标高2到4倍的流量(当然具体性能还跟业务复杂性相关）
* 两台docker虚拟机，配置是2核8G内存，STAT硬盘400G。

| 指标     | 单位      | 监控                                       |
| :----- | :------ | :--------------------------------------- |
| 流量     | TPS/min | <img src="docs/qps_min.png" width="600" height = "300" alt="图片名称" align=center /> |
| 平均耗时   | ms      | <img src="docs/cost.png" width="600" height = "300" alt="图片名称" align=center /> |
| 内存使用率  | 8G-%    | <img src="docs/mem_use.png" width="600" height = "200" alt="图片名称" align=center /> |
| CPU使用率 | 100%    | <img src="docs/cpu_use.png" width="600" height = "200" alt="图片名称" align=center /> |

# micrboot怎么使用
### maven配置
```
    <!-- 依赖 -->
    <dependency>
        <groupId>org.springframework</groupId>
        <artifactId>spring-context</artifactId>
    </dependency>
    <dependency>
        <groupId>io.netty</groupId>
        <artifactId>netty-all</artifactId>
    </dependency>
    <dependency>
        <groupId>com.fasterxml.jackson.core</groupId>
        <artifactId>jackson-databind</artifactId>
   </dependency>
   <dependency>
       <groupId>org.slf4j</groupId>
        <artifactId>slf4j-api</artifactId>
   </dependency>
```
```
    <dependency>
      <groupId>com.zhuanglide</groupId>
      <artifactId>micrboot</artifactId>
      <version>1.0.1</version>
    </dependency>
```
### spring 配置 
* 可参照 micrboot-demo/src/main/resources/api.xml
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

### 输入helloworld
```
    @ApiCommand
    public class HelloWorldCommand{
        @ApiMethod("/hello/world")
        public String helloWorld(){
            return "Hello World";
        }    
    }
    
    curl http://localhost:8080/hello/world   
    #输出 Hello World
```
### 输出Json 
```
    @ApiCommand("json")
    public class JsonCommand{
        @ApiMethod("/test",httpMethod = ApiMethod.HttpMethod.POS)
        public ModelAndView test(@ApiParam("name")String name,@ApiParam("id")int id){
            ModelAndView mv = new ModelAndView("jsonView");
            Map<String, Object> res = new HashMap<String, Object>();
            res.put("id",id);
            res.put("name", name);
            mv.setResult(mv);
            return mv;
        }    
    }
    
    curl -d 'name=tomcat&id=100' http://localhost:8080/json/test   
    #输出 {"name":"tomcat","id":100}
```
### 更多例子和用法
请看micrboot-demo

# 拦截器
* 类springMVC拦截器
  增加拦截器，需要在spring配置文件中显示调用

```
  <bean class="com.zhuanglide.micrboot.demo.interceptor.TestInterceptor">  
      <property name="order" value="1"/>
  </bean>
    
    public class TestInterceptor extends AbstractApiInterceptor {
        int order ; //拦截器执行顺序
        //分发之前，甚至可以更改最终invoke的接口
        @Override
        public boolean preDispatch(HttpContextRequest request, HttpContextResponse response) {
            System.out.println("TestInterceptor preDispatch url="+request.getRequestUrl());
            return super.preDispatch(request, response);
        }
        
        //执行invoke方法之前，可以更改，增加删除参数值
        @Override
        public boolean postHandler(ApiCommandMapping mapping, HttpContextRequest request, HttpContextResponse response) {
            System.out.println("TestInterceptor postHandler url="+request.getRequestUrl());
            return super.postHandler(mapping, request, response);
        }
        
        //执行invoke方法之后，可以修改返回值
        @Override
        public void afterHandle(ApiCommandMapping mapping, Object modelView, HttpContextRequest request, HttpContextResponse response, Throwable throwable) {
            System.out.println("TestInterceptor afterHandle url="+request.getRequestUrl());
            super.afterHandle(mapping, modelView, request, response, throwable);
        }
    }
```

# 多接口合并

* 设置spring配置文件 需要增加一个设置，获取apiDispatcher内部处理类，调用doProcess方法
```
    <bean class="com.zhuanglide.micrboot.mvc.ApiDispatcher"/>
```
具体用法参考micrboot-demo的 BatchCommndTest类
