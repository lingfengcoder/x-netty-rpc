package com.lingfeng.rpc.proxy;

import com.lingfeng.rpc.proxy.handler.RpcClientProxyHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * @RpcClient 接口的动态生产 bean 的工厂
 * bean
 */
@Slf4j
public class RpcClientBeanFactory implements FactoryBean, ApplicationContextAware {
    private ApplicationContext applicationContext;
    private Class<?> targetClazz;
    private Object target;

    public RpcClientBeanFactory() {
    }

    public void setTargetClazz(Class<?> targetClazz) {
        this.targetClazz = targetClazz;
    }

    @Override
    public Class<?> getObjectType() {
        return target.getClass();
    }

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public Object getObject() {
        if (this.target != null) {
            log.info("getObject this.target={}", target);
            return this.target;
        } else {
            //通过jdk动态代理生成bean 代理方法为: RpcClientProxyHandler
            this.target = JdkDynamicProxyUtil.proxyInvoke(targetClazz, new RpcClientProxyHandler());
            log.info("JdkDynamicProxyUtil  target={}", target);
            return this.target;
        }
    }
}
