package com.lingfeng.rpc.invoke;

import com.lingfeng.rpc.proxy.JdkDynamicProxyUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

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

    public Class<?> getObjectType() {
        return target.getClass();
    }

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public Object getObject() {
        if (this.target != null) {
            log.info("getObject this.target={}", target);
            return this.target;
        } else {
            this.target = JdkDynamicProxyUtil.proxyInvoke(targetClazz, new RemoteProcess());
            log.info("JdkDynamicProxyUtil  target={}", target);
            return this.target;
        }
    }
}
