package com.lingfeng.rpc.proxy.handler;


import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import com.lingfeng.rpc.ann.RpcClient;
import com.lingfeng.rpc.data.RpcInvokeFrame;
import com.lingfeng.rpc.handler.RpcConsumer;
import com.lingfeng.rpc.handler.SpringServerProxyInvokeHandler;
import com.lingfeng.rpc.invoke.CallbackPosition;
import com.lingfeng.rpc.invoke.RemoteInvoke;
import com.lingfeng.rpc.proxy.ProxySender;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Calendar;

/**
 * @Auther: wz
 * @Date: 2022/6/16 16:04
 * @Description:
 */
@Slf4j
public class RpcClientProxyHandler implements InvocationHandler {
    //是否是阻塞模式
    private static final ThreadLocal<Boolean> blockInvokeThreadLocal = new ThreadLocal<>();

    public static void setBlockModel() {
        blockInvokeThreadLocal.set(true);
    }

    public static void release() {
        blockInvokeThreadLocal.remove();
    }

    public static boolean isBlockModel() {
        Boolean flag = blockInvokeThreadLocal.get();
        return flag != null && flag;
    }

    //生成消息序列号
    public static String generateSeq() {
        return System.currentTimeMillis() + IdUtil.randomUUID();
    }

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
        {//参数处理
            req.setSeq(generateSeq());
            //bean名称
            req.setBeanName(value);
            //目标方法名称
            req.setMethodName(method.getName());
            //参数
            req.setArguments(args);
            //设置回调坐标
            RpcInvokeFrame callbackPosition = CallbackPosition.getCallbackPosition();
            if (callbackPosition != null) {
                req.setRetPosition(callbackPosition);
                //清除threadLocal
                CallbackPosition.removeCallbackPosition();
            }
            if (args != null) {
                //参数类型
                Class<?>[] clazz = new Class[args.length];
                for (int i = 0; i < args.length; i++) {
                    clazz[i] = args.getClass();
                }
                req.setParameterTypes(clazz);
            }
            //设置同步模式
            req.setSync(isBlockModel());
        }
        //远程服务调用
        ProxySender proxySender = RemoteInvoke.getInstance().getSender();
        proxySender.writeAndFlush(req);
        {//结果处理
            //如果是同步模式，则等待结果
            if (isBlockModel()) {
                //清除threadLocal
                release();
                //wait
                log.info("thread={} 同步阻塞模式 等待", Thread.currentThread());
                return RpcConsumer.waitFor(req.getSeq(), new Object());
            } else {
                //异步模式就直接返回null
                return null;
            }
        }
    }
}
