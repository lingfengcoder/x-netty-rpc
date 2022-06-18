package com.lingfeng.rpc.invoke;


import com.lingfeng.rpc.client.nettyclient.BizNettyClient;
import com.lingfeng.rpc.data.RpcInvokeFrame;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * @Auther: wz
 * @Date: 2022/6/16 16:04
 * @Description:过程
 */
@Slf4j
public class RemoteProcess implements InvocationHandler {


    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.getName().equals("toString")) {
            return "RegisterAction no toString ";
        }
        log.info("RemoteProcess proxy={} invoke method={} args={}", proxy, method.getName(), args);
        log.info("RemoteProcess thread={}", Thread.currentThread());
        RpcInvokeFrame req = new RpcInvokeFrame();
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
