package com.lingfeng.rpc.invoke;

import com.lingfeng.rpc.data.AnnHandler;
import com.lingfeng.rpc.data.RpcInvokeFrame;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.function.Consumer;
import java.util.stream.Stream;

@Slf4j
public class RpcInvokeProxy {

    // 线程内置的channel
    private static final ThreadLocal<Channel> channelThreadLocal = new ThreadLocal<>();

    public static void setChannel(Channel channel) {
        channelThreadLocal.set(channel);
    }

    public static Channel getChannel() {
        return channelThreadLocal.get();
    }

    public static void removeChannel() {
        channelThreadLocal.remove();
    }

    public static Object invoke(Channel channel, Consumer<Object> postHandler, RpcInvokeFrame frame) {
        if (channel != null) {
            setChannel(channel);
        }
        return invoke(postHandler, frame);
    }

    private static Object invoke(Consumer<Object> postHandler, RpcInvokeFrame frame) {
        //从方法集中找到bean和method
        AnnHandler handler = RpcBeanRegister.getHandler(frame.getMethodName());
        try {
            if (handler != null) {
                //执行方法
                Object bean = handler.getBean();
                Method method = handler.getMethod();
                Object result = null;
                try {
                    Object[] arguments = frame.getArguments();
                    //不进行方法重载检查，因为在RpcBeanRegister里面不允许方法名称重复
                    Class<?>[] methodParameterTypes = method.getParameterTypes();
                    Class<?>[] frameParameterTypes = frame.getParameterTypes();
                    result = method.invoke(bean, arguments);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    log.error(e.getMessage(), e);
                }
                if (postHandler != null) {
                    postHandler.accept(result);
                }
            } else {
                log.error("没有找到 @RpcHandler(\"" + frame.getMethodName() + "\")的处理器");
            }
        } finally {
            //清理channel
            removeChannel();
        }
        return null;
    }

    public static Object invoke(Channel channel, Consumer<Object> postHandler, String name, Object... param) {
        if (channel != null) {
            setChannel(channel);
        }
        return invoke(postHandler, name, param);
    }

    private static Object invoke(Consumer<Object> postHandler, String name, Object... param) {
        //从方法集中找到bean和method
        AnnHandler handler = RpcBeanRegister.getHandler(name);
        try {
            if (handler != null) {
                //执行方法
                Object bean = handler.getBean();
                Method method = handler.getMethod();
                Object result = null;
                try {
                    result = method.invoke(bean, param);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    log.error(e.getMessage(), e);
                }
                if (postHandler != null) {
                    postHandler.accept(result);
                }
            } else {
                log.error("没有找到 @RpcHandler(\"" + name + "\")的处理器");
            }
        } finally {
            //清理channel
            removeChannel();
        }
        return null;
    }

    private static Object invoke(String name, Object... param) {
        try {
            //从方法集中找到bean和method
            AnnHandler handler = RpcBeanRegister.getHandler(name);
            if (handler != null) {
                //执行方法
                Object bean = handler.getBean();
                Method method = handler.getMethod();
                Object result = null;
                try {
                    result = method.invoke(bean, param);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    log.error(e.getMessage(), e);
                }
                return result;
            } else {
                log.error("没有找到 @RpcHandler(\"" + name + "\")的处理器");
            }
        } finally {
            //清理channel
            removeChannel();
        }
        return null;
    }
}
