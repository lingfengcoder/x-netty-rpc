//package com.lingfeng.rpc.invoke;
//
//import com.lingfeng.rpc.ann.RpcClient;
//import com.lingfeng.rpc.proxy.JdkDynamicProxyUtil;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.BeansException;
//import org.springframework.beans.factory.DisposableBean;
//import org.springframework.beans.factory.SmartInitializingSingleton;
//import org.springframework.context.ApplicationContext;
//import org.springframework.context.ApplicationContextAware;
//import org.springframework.context.annotation.Configuration;
//
//import java.util.Map;
//import java.util.concurrent.ConcurrentHashMap;
//
///**
// * @Auther: wz
// * @Date: 2022/6/16 13:41
// * 1.扫描所有的远程服务接口
// * 2.动态代理生成对象
// * 3.将代理对象缓存，避免反复生成
// */
//@Slf4j
//@Configuration
//public class RpcTargetRegister implements ApplicationContextAware, SmartInitializingSingleton, DisposableBean {
//    private final Map<Class<?>, Object> dynamicProxyCache = new ConcurrentHashMap<>();
//
//    @Override
//    public void destroy() throws Exception {
//        log.info("RpcClientRegister destroy");
//    }
//
//    public Map<Class<?>, Object> getDynamicProxyCache() {
//        return dynamicProxyCache;
//    }
//
//    @Override
//    public void afterSingletonsInstantiated() {
//        Map<String, Object> beans = applicationContext.getBeansWithAnnotation(RpcClient.class);
//        for (Map.Entry<String, Object> item : beans.entrySet()) {
//            Object bean = item.getValue();
//            Object proxyBean = JdkDynamicProxyUtil.proxyInvoke(bean.getClass(), new RemoteProcess());
//            dynamicProxyCache.put(bean.getClass(), proxyBean);
//        }
//    }
//
//    private static ApplicationContext applicationContext;
//
//    @Override
//    public void setApplicationContext(ApplicationContext ac) throws BeansException {
//        applicationContext = ac;
//    }
//}
