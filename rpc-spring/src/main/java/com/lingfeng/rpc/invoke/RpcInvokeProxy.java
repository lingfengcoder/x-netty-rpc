package com.lingfeng.rpc.invoke;

import com.lingfeng.rpc.data.BeanHandler;
import com.lingfeng.rpc.data.RpcInvokeFrame;
import com.lingfeng.rpc.proxy.RpcComponentRegister;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.Consumer;

@Slf4j
/**
 * 目标方法反射执行类。用于在获取到 @RecClient 的消息后，通过反射寻找目标方法并执行
 * 由于可能目标方法是线程池执行的，如果目标方法需要获取channel，需要提前通过 channelThreadLocal 进行设置
 */
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
        //异步模式，切回调坐标不为空
        if (!frame.isSync() && frame.getRetPosition() != null) {
            CallbackPosition.setCallbackPosition(frame.getRetPosition());
        }
        return invoke(postHandler, frame);
    }

    private static Object invoke(Consumer<Object> postHandler, RpcInvokeFrame frame) {
        //从方法集中找到bean和method
        String beanName = frame.getBeanName();
        BeanHandler handler = RpcComponentRegister.getHandler(beanName);
        try {
            if (handler != null) {
                //执行方法
                Object bean = handler.getBean();
                Method method = handler.getMethod().get(frame.getMethodName());
                if (method != null) {
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
                    log.error(beanName + "中没有找到 @RpcHandler(\"" + frame.getMethodName() + "\")的处理器");
                }
            } else {
                log.error("没有找到 @RpcComponent(\"" + beanName + "\")的bean");
            }
        } finally {
            //清理channel
            removeChannel();
            CallbackPosition.removeCallbackPosition();
        }
        return null;
    }

    public static Object invoke(Channel channel, Consumer<Object> postHandler, String beanName, String methodName, Object... param) {
        if (channel != null) {
            setChannel(channel);
        }
        return invoke(postHandler, beanName, methodName, param);
    }

    private static Object invoke(Consumer<Object> postHandler, String beanName, String methodName, Object... param) {
        //从方法集中找到bean和method
        BeanHandler handler = RpcComponentRegister.getHandler(beanName);
        try {
            if (handler != null) {
                //执行方法
                Object bean = handler.getBean();
                Method method = handler.getMethod().get(methodName);
                if (method != null) {
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
                    log.error(beanName + "中没有找到 @RpcHandler(\"" + methodName + "\")的处理器");
                }
            } else {
                log.error("没有找到 @RpcComponent(\"" + beanName + "\")的bean");
            }
        } finally {
            //清理channel
            removeChannel();
            CallbackPosition.removeCallbackPosition();
        }
        return null;
    }
}
