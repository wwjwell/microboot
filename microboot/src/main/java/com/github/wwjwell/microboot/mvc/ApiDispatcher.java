package com.github.wwjwell.microboot.mvc;

import com.github.wwjwell.microboot.http.HttpContextRequest;
import com.github.wwjwell.microboot.http.HttpContextResponse;
import com.github.wwjwell.microboot.http.MediaType;
import com.github.wwjwell.microboot.mvc.annotation.ApiMethod;
import com.github.wwjwell.microboot.mvc.interceptor.ApiInterceptor;
import com.github.wwjwell.microboot.util.HttpUtils;
import com.github.wwjwell.microboot.mvc.annotation.ApiCommand;
import com.github.wwjwell.microboot.mvc.resolver.ApiMethodParamResolver;
import com.github.wwjwell.microboot.mvc.resolver.ExceptionResolver;
import com.github.wwjwell.microboot.mvc.resolver.ViewResolver;
import com.github.wwjwell.microboot.mvc.resolver.param.ApiMethodPathVariableResolver;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.cglib.proxy.Proxy;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.util.*;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.*;

/**
 * Created by wwj on 17/3/2.
 */
public class ApiDispatcher implements ApplicationContextAware,InitializingBean {
    private Map<String, Map<ApiMethod.HttpMethod, ApiMethodMapping>> commandMap;
    private Map<String, Map<ApiMethod.HttpMethod, ApiMethodMapping>> cachePathMap = new HashMap<String, Map<ApiMethod.HttpMethod, ApiMethodMapping>>();
    private AntPathMatcher matcher = new AntPathMatcher();
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private static final String DEFAULT_STRATEGIES_PATH = "DefaultStrategies.properties";
    private static final Properties defaultStrategies;
    private ApplicationContext context;
    private List<ApiInterceptor> apiInterceptors;
    private List<ViewResolver> viewResolvers;
    private List<ApiMethodParamResolver> apiMethodParamResolvers;
    private List<ExceptionResolver> exceptionResolvers;
    private LocalVariableTableParameterNameDiscoverer paramNamesDiscoverer = new LocalVariableTableParameterNameDiscoverer();
    static {
        try {// 加载默认配置
            ClassPathResource resource = new ClassPathResource(DEFAULT_STRATEGIES_PATH, ApiDispatcher.class);
            defaultStrategies = PropertiesLoaderUtils.loadProperties(resource);
        } catch (IOException ex) {
            throw new IllegalStateException("Could not load 'DefaultStrategies.properties': " + ex.getMessage());
        }
    }

    /**
     * 分发
     * @param request
     * @return
     */
    public ApiMethodMapping dispatcher(HttpContextRequest request) throws Exception{
        String methodValue = request.getHttpMethod();
        ApiMethod.HttpMethod httpMethod = ApiMethod.HttpMethod.ALL;
        //根据method 和 path分发请求
        for (ApiMethod.HttpMethod _httpMethod : ApiMethod.HttpMethod.values()) {
            if(_httpMethod.equals(methodValue)){
                httpMethod = _httpMethod;
                break;
            }
        }

        return findApiMethodMapping(request.getRequestUrl(),httpMethod);
    }

    /**
     * core method
     * @param request
     * @param response
     * @throws Exception
     */
    public void doService(HttpContextRequest request, HttpContextResponse response) throws Exception {
        HandlerExecuteChain chain = new HandlerExecuteChain(apiInterceptors,exceptionResolvers);
        Throwable handlerEx = null;
        try {
            try {
                doProcess0(request, response, chain, true);
            } catch (Exception e) {
                handlerEx = e;
            } catch (Throwable throwable) {
                handlerEx = throwable;
            }
            //gen view
            processDispatchResult(chain, request, response, handlerEx);
        } catch (Exception ex) {
            chain.triggerException(request, response, ex);
        } catch (Throwable ex) {
            chain.triggerException(request, response, ex);
        }
    }

    /**
     * do process用于外部调用
     */
    public Object doProcess(HttpContextRequest request, HttpContextResponse response) throws Exception{
        return doProcess0(request, response, null, true);
    }

    /**
     * do process用于外部调用
     * @param request
     * @param response
     * @param withInterceptor if true ,interceptors are usable,false -> disabled
     * @return
     * @throws Exception
     */
    public Object doProcess(HttpContextRequest request, HttpContextResponse response,boolean withInterceptor) throws Exception{
        return doProcess0(request, response, null, withInterceptor);
    }

    /**
     * do doProcess0 ,just
     * @param request
     * @param response
     * @param chain
     * @param withInterceptor if true ,interceptors are usable,false -> disabled
     * @return
     * @throws Exception
     */
    public Object doProcess0(HttpContextRequest request, HttpContextResponse response, HandlerExecuteChain chain, boolean withInterceptor) throws Exception{
        if(null == chain) {
            chain = new HandlerExecuteChain(apiInterceptors, exceptionResolvers);
        }

        ApiMethodMapping mapping;
        try {
            if(withInterceptor) {
                //pre dispatch ,you can do some to change the invoke method
                if (!chain.applyPreDispatch(request, response)) {
                    return null;
                }
            }
            //do dispatcher, url->method
            mapping = dispatcher(request);
            chain.setMapping(mapping); //set mapping
            if (null == mapping) {
                logger.error("can't find match for method={},path={}", request.getHttpMethod(), request.getRequestUrl());
                response.setStatus(HttpResponseStatus.NOT_FOUND);
                return null;
            }
            if(withInterceptor && !chain.applyPostHandle(request, response)) {
                //post handler,you can change params
                return null;
            }
            //invoke & handler
            Object result = null;
            Throwable ex = null;
            try {
                result = handler(mapping, request, response);
                chain.setResult(result);
            } catch (Throwable throwable) {
                ex = throwable;
            }
            if (withInterceptor) {
                chain.applyAfterHandler(request, response, ex);
            }
            return result;
        } catch (Exception e) {
            throw e;
        } catch (Throwable cause) {
            throw new Exception(cause);
        }
    }


    private void processDispatchResult(HandlerExecuteChain chain, HttpContextRequest request, HttpContextResponse response,Throwable throwable) throws Throwable {
        if (throwable == null) {
            // Did the handler return a view to render?
            if (chain.getResult() != null) {
                if (!render(chain.getResult(), request, response)) {
                    response.setStatus(HttpResponseStatus.NOT_FOUND);
                    response.setContent("can't find view with path="+request.getRequestUrl());
                    logger.warn("no view found with path={}", chain.getMapping().getUrlPattern());
                }
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug("no modelView returned to ApiDispatcher with path={},", request.getRequestUrl());
                }
            }
        }
        chain.triggerAfterCompletion(request, response, throwable);
    }

    protected boolean render(Object result, HttpContextRequest request, HttpContextResponse response) throws Exception {
        boolean resolver = false;
        if (null != result && result instanceof ModelAndView) {
            ModelAndView mv = (ModelAndView) result;
            if(mv.getMediaType()!=null) {
                String contentType = request.getHeader(HttpHeaderNames.CONTENT_TYPE);
                if (null != contentType) {
                    mv.setMediaType(MediaType.valueOf(contentType));
                }
            }
        }
        for (ViewResolver viewResolver : this.viewResolvers) {
            ModelAndView mv = viewResolver.resolve(result);
            if (null != mv) {
                viewResolver.render(mv, request, response);
                if (null != viewResolver.getContentType() && !response.containsHeader(HttpHeaderNames.CONTENT_TYPE)) {
                    response.addHeader(HttpHeaderNames.CONTENT_TYPE, viewResolver.getContentType());
                }
                resolver = true;
                break;
            }
        }
        return resolver;
    }

    /**
     * init
     * @param context
     */
    protected void initStrategies(ApplicationContext context) {
        initApiInterceptor(context);
        initApiMethodParamResolvers(context);
        initViewResolver(context);
        initExceptionResolvers(context);
        Assert.isTrue(loadApiCommand(context));
    }

    /**
     * load from annotation
     * @param context
     * @return
     */
    private boolean loadApiCommand(ApplicationContext context) {
        if (context != null) {
            commandMap = new HashMap<String, Map<ApiMethod.HttpMethod, ApiMethodMapping>>();
            Map<String, Object> objectMap = context.getBeansWithAnnotation(ApiCommand.class);
            for (Map.Entry<String, Object> entry : objectMap.entrySet()) {
                try {
                    Object bean = entry.getValue();
                    if (AopUtils.isAopProxy(bean)) {
                        bean = AopUtils.getTargetClass(bean);
                    }
                    Method[] methodArray = bean.getClass().getMethods();
                    ApiCommand apiCommand = bean.getClass().getAnnotation(ApiCommand.class);
                    for (Method method : methodArray) {
                        ApiMethod apiMethod = AnnotationUtils.findAnnotation(method, ApiMethod.class);
                        if (apiMethod != null) {
                            ApiMethodMapping apiCommandMapping = new ApiMethodMapping();
                            apiCommandMapping.setBean(entry.getValue());
                            apiCommandMapping.setProxyTargetBean(bean);
                            apiCommandMapping.setMethod(method);
                            apiCommandMapping.setUrlPattern(HttpUtils.joinOptimizePath(apiCommand.value(), apiMethod.value()));
                            apiCommandMapping.setParamNames(paramNamesDiscoverer.getParameterNames(method));
                            apiCommandMapping.setParamAnnotations(method.getParameterAnnotations());
                            apiCommandMapping.setParameterTypes(method.getParameterTypes());
                            Map<ApiMethod.HttpMethod, ApiMethodMapping> requestMethodMap = commandMap.get(apiCommandMapping.getUrlPattern());
                            if (requestMethodMap == null) {
                                requestMethodMap = new HashMap<ApiMethod.HttpMethod, ApiMethodMapping>();
                                commandMap.put(apiCommandMapping.getUrlPattern(), requestMethodMap);
                            }
                            requestMethodMap.put(apiMethod.httpMethod(), apiCommandMapping);
                        }
                    }

                } catch (Exception e) {
                    logger.error("init error", e);
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * findApiMethodMapping from cache or create new when first init
     */
    public ApiMethodMapping findApiMethodMapping(String url, ApiMethod.HttpMethod requestMethod) {
        Map<ApiMethod.HttpMethod, ApiMethodMapping> mapping = cachePathMap.get(url);
        ApiMethodMapping apiMethodMapping = null;
        //删除锁 synchronized, 并发是幂等的，加锁没有任何意义
        //See https://github.com/wwjwell/microboot/issues/4
        //
        if (null == mapping) {
            for (Map.Entry<String, Map<ApiMethod.HttpMethod, ApiMethodMapping>> entry : commandMap.entrySet()) {
                if(matcher.match(entry.getKey(), url)) {
                    mapping = entry.getValue();
                    if(checkCache()) {
                        cachePathMap.put(url, mapping);
                    }
                    break;
                }
            }
        }

        if(null != mapping){
            //restful comp
            apiMethodMapping = mapping.get(requestMethod);
            if(null == apiMethodMapping && requestMethod != ApiMethod.HttpMethod.ALL) {
                apiMethodMapping = mapping.get(ApiMethod.HttpMethod.ALL);
            }
        }

        return apiMethodMapping;
    }
    private boolean checkCache(){
        return cachePathMap.size()<50000;
    }


    protected void initViewResolver(ApplicationContext context){
        this.viewResolvers = getDefaultStrategies(context, ViewResolver.class);
        Map<String, ViewResolver> matchingBeans =
                BeanFactoryUtils.beansOfTypeIncludingAncestors(context, ViewResolver.class, true, false);
        if (!matchingBeans.isEmpty()) {
            this.viewResolvers.addAll(matchingBeans.values());
        }
        AnnotationAwareOrderComparator.sort(this.viewResolvers);
    }

    protected void initApiMethodParamResolvers(ApplicationContext context){
        this.apiMethodParamResolvers = getDefaultStrategies(context, ApiMethodParamResolver.class);
        Map<String, ApiMethodParamResolver> matchingBeans =
                BeanFactoryUtils.beansOfTypeIncludingAncestors(context, ApiMethodParamResolver.class, true, false);
        if (!matchingBeans.isEmpty()) {
            this.apiMethodParamResolvers.addAll(matchingBeans.values());
        }
        AnnotationAwareOrderComparator.sort(this.apiMethodParamResolvers);
    }

    protected void initExceptionResolvers(ApplicationContext context) {
        Map<String, ExceptionResolver> matchingBeans =
                BeanFactoryUtils.beansOfTypeIncludingAncestors(context, ExceptionResolver.class, true, false);
        if (!matchingBeans.isEmpty()) {
            this.exceptionResolvers = new ArrayList<ExceptionResolver>(matchingBeans.values());
        }

        if (this.exceptionResolvers == null) {
            this.exceptionResolvers = getDefaultStrategies(context, ExceptionResolver.class);
            if (logger.isDebugEnabled()) {
                logger.debug("No ExceptionResolver found ,using default");
            }
        }
        AnnotationAwareOrderComparator.sort(this.exceptionResolvers);
    }


    protected void initApiInterceptor(ApplicationContext context){
        Map<String, ApiInterceptor> matchingBeans =
                BeanFactoryUtils.beansOfTypeIncludingAncestors(context, ApiInterceptor.class, true, false);
        if (!matchingBeans.isEmpty()) {
            this.apiInterceptors = new ArrayList<ApiInterceptor>(matchingBeans.values());
            AnnotationAwareOrderComparator.sort(this.apiInterceptors);
        }
    }


    @Override
    public void afterPropertiesSet() throws Exception {
        initStrategies(context);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
    }

    protected <T> T getDefaultStrategy(ApplicationContext context, Class<T> strategyInterface) {
        List<T> strategies = getDefaultStrategies(context, strategyInterface);
        if (strategies.size() != 1) {
            throw new BeanInitializationException(
                    "ApiDispatcher needs exactly 1 strategy for interface [" + strategyInterface.getName() + "]");
        }
        return strategies.get(0);
    }

    protected <T> List<T> getDefaultStrategies(ApplicationContext context, Class<T> strategyInterface) {
        String key = strategyInterface.getName();
        String value = defaultStrategies.getProperty(key);
        if (value != null) {
            String[] classNames = StringUtils.commaDelimitedListToStringArray(value);
            List<T> strategies = new ArrayList<T>(classNames.length);
            for (String className : classNames) {
                try {
                    Class<?> clazz = ClassUtils.forName(className, ApiDispatcher.class.getClassLoader());
                    Object strategy = createDefaultStrategy(context, clazz);
                    strategies.add((T) strategy);
                }
                catch (ClassNotFoundException ex) {
                    throw new BeanInitializationException(
                            "Could not find ApiDispatcher's default strategy class [" + className +
                                    "] for interface [" + key + "]", ex);
                }
                catch (LinkageError err) {
                    throw new BeanInitializationException(
                            "Error loading ApiDispatcher's default strategy class [" + className +
                                    "] for interface [" + key + "]: problem with class file or dependent class", err);
                }
            }
            return strategies;
        }
        else {
            return new LinkedList<T>();
        }
    }

    protected Object createDefaultStrategy(ApplicationContext context, Class<?> clazz) {
        return context.getAutowireCapableBeanFactory().createBean(clazz);
    }


    /**
     * 处理
     * @return
     */
    private Object handler(ApiMethodMapping mapping,HttpContextRequest request,HttpContextResponse response) throws Throwable {
        Method method = mapping.getMethod();
        Type[] parameterTypes = mapping.getParameterTypes();
        Annotation[][] annotations = mapping.getParamAnnotations();
        String[] paramNames = mapping.getParamNames();
        Object[] values = new Object[parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; i++) {
            Type type = parameterTypes[i];
            Annotation[] paramAnnotations = annotations[i];
            String paramName = paramNames[i];
            ApiMethodParam apiMethodParam = new ApiMethodParam();
            apiMethodParam.setMethod(method);
            apiMethodParam.setParamAnnotations(paramAnnotations);
            apiMethodParam.setParamType(type);
            apiMethodParam.setParamName(paramName);
            Object paramObjectValue = null;

            if(!ObjectUtils.isEmpty(apiMethodParamResolvers)){
                for (ApiMethodParamResolver resolver : apiMethodParamResolvers) {
                    if (resolver.support(apiMethodParam)) {
                        //PathVariable need doPathVariableParse
                        if(resolver instanceof ApiMethodPathVariableResolver){
                            resolver.prepare(mapping, request, matcher);
                        }
                        paramObjectValue = resolver.getParamObject(apiMethodParam, request, response);
                        break;
                    }
                }
            }

            values[i] = paramObjectValue;
        }
        Object bean = mapping.getBean();
        Object result;
        //invoke
        try {
            if (AopUtils.isAopProxy(bean)) {
                if (AopUtils.isJdkDynamicProxy(bean)) {
                    result = Proxy.getInvocationHandler(bean).invoke(bean, method, values);
                } else { //cglib
                    result = method.invoke(bean, values);
                }
            } else {
                result = ReflectionUtils.invokeMethod(method, bean, values);
            }
            return result;
        } catch (Exception e) {
            StringBuffer val = new StringBuffer();
            for (int i = 0; i < parameterTypes.length; i++) {
                val.append(parameterTypes).append(":").append(values[i]).append(";");
            }
            logger.error("invoke exception,object="+bean+",method="+method+",values"+val.toString(), e);
            throw new Exception("invoke exception,url=" + request.getRequestUrl());
        }
    }
}
