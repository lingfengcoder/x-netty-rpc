package com.lingfeng.rpc.invoke;

import com.lingfeng.rpc.constant.Cmd;
import com.lingfeng.rpc.data.Frame;
import com.lingfeng.rpc.data.RpcInvokeBean;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * @Auther: wz
 * @Date: 2022/6/16 16:04
 * @Description:过程
 */
public class RemoteProcess implements InvocationHandler {



    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        RpcInvokeBean req = new RpcInvokeBean();

        //目标方法名称
        req.setMethodName(method.getName());
        //参数
        req.setArguments(args);
        //参数类型
        Class<?>[] clazz = new Class[args.length];
        for (int i = 0; i < args.length; i++) {
            clazz[i] = args.getClass();
        }
        req.setParameterTypes(clazz);

        writeAndFlush(ctx.channel(), resp, Cmd.RESPONSE);

        return null;
    }
}
