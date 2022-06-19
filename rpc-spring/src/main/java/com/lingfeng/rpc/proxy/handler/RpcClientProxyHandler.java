package com.lingfeng.rpc.proxy.handler;


import com.lingfeng.rpc.ann.RpcClient;
import com.lingfeng.rpc.data.RpcInvokeFrame;
import com.lingfeng.rpc.invoke.RemoteInvoke;
import com.lingfeng.rpc.proxy.ProxySender;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * @Auther: wz
 * @Date: 2022/6/16 16:04
 * @Description:
 */
@Slf4j
public class RpcClientProxyHandler implements InvocationHandler {


    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.getName().equals("toString")) {
            log.info("RemoteProcess invoke toString() method");
            return "RegisterAction no toString ";
        }
        Class<?> beanClass = method.getDeclaringClass();
        RpcClient[] descRpcClients = beanClass.getAnnotationsByType(RpcClient.class);
        RpcClient descRpcClient = descRpcClients[0];
        //目标类
        String value = descRpcClient.value();

        log.info("RemoteProcess thread={} proxy={} invoke method={} args={}", Thread.currentThread(), proxy, method.getName(), args);
        RpcInvokeFrame req = new RpcInvokeFrame();
        //bean名称
        req.setBeanName(value);
        //目标方法名称
        req.setMethodName(method.getName());
        //参数
        req.setArguments(args);
        if (args != null) {
            //参数类型
            Class<?>[] clazz = new Class[args.length];
            for (int i = 0; i < args.length; i++) {
                clazz[i] = args.getClass();
            }
            req.setParameterTypes(clazz);
        }
        //远程服务调用
        ProxySender proxySender = RemoteInvoke.getInstance().getSender();
        proxySender.writeAndFlush(req);
        return null;
    }
}
