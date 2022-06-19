package com.lingfeng.rpc.proxy;

import com.lingfeng.rpc.ann.RpcComponent;
import com.lingfeng.rpc.ann.RpcHandler;
import com.lingfeng.rpc.data.BeanHandler;
import com.lingfeng.rpc.util.StringUtils;
import com.lingfeng.rpc.util.relfect.ReflectUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @RpcComponent 修饰的bean进行缓存并代理，在收到目标消息后，可以通过本类进行检索出目标方法
 */
@Slf4j
@Component
public class RpcComponentRegister implements ApplicationContextAware, SmartInitializingSingleton, DisposableBean {


    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        RpcComponentRegister.applicationContext = applicationContext;
    }

    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    // start
    @Override
    public void afterSingletonsInstantiated() {
        log.info("============= afterSingletonsInstantiated=============");
        annHandlerRegister(applicationContext);
    }

    // destroy
    @Override
    public void destroy() {
    }

    //存放所有带注解的bean
    private final static ConcurrentHashMap<String, BeanHandler> beanHandlerMap = new ConcurrentHashMap<>();

    public static BeanHandler getHandler(String name) {
        return beanHandlerMap.get(name);
    }

    private void annHandlerRegister(ApplicationContext applicationContext) {
        if (applicationContext == null) {
            return;
        }
        // 找到rpc的bean
        Map<String, Object> beansMap = applicationContext.getBeansWithAnnotation(RpcComponent.class);
        Collection<Object> beans = beansMap.values();
        //找到rpc的method 和name
        for (Object bean : beans) {
            RpcComponent[] annotations = bean.getClass().getAnnotationsByType(RpcComponent.class);
            RpcComponent annotation = annotations[0];
            String beanName = annotation.value();
            if (StringUtils.isEmpty(beanName)) {
                beanName = bean.getClass().getSimpleName();
            }
            //check
            beanNameValidate(beanName);
            BeanHandler beanHandler = new BeanHandler();
            HashMap<String, Method> methodMap = new HashMap<>();
            beanHandler.setBean(bean);
            beanHandler.setBeanName(beanName);
            beanHandler.setMethod(methodMap);
            //获取全部方法
            HashSet<Method> allMethod = ReflectUtils.getAllMethod(bean.getClass());
            for (Method method : allMethod) {
                //获取方法上的注解
                RpcHandler rpcHandler = method.getAnnotation(RpcHandler.class);
                if (rpcHandler != null) {
                    String methodName = rpcHandler.value();
                    if (StringUtils.isEmpty(methodName)) {
                        methodName = method.getName();
                    }
                    methodMap.put(methodName, method);
                }
            }
            beanHandlerMap.put(beanName, beanHandler);
        }
    }

    //判断缓存中是否已经包含了beanName(beanName是否重复)
    private void beanNameValidate(String beanName) {
        if (beanHandlerMap.containsKey(beanName)) {
            throw new RuntimeException("@RpcComponent value=" + beanName + "的bean重复了!");
        }
    }

}
