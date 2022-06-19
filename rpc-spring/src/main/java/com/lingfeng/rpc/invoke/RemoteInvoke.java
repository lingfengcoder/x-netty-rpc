package com.lingfeng.rpc.invoke;

import cn.hutool.extra.spring.SpringUtil;
import com.lingfeng.rpc.base.Sender;
import com.lingfeng.rpc.proxy.JdkDynamicProxyUtil;
import com.lingfeng.rpc.proxy.ProxySender;
import com.lingfeng.rpc.proxy.handler.RpcClientProxyHandler;
import io.netty.channel.Channel;
import lombok.Setter;

import java.util.function.Supplier;


/**
 * @Auther: wz
 * @Date: 2022/6/16 16:13
 * @Description: #@RpcClient 注解的代理类在执行方法之前，需要获取对应的channel,
 * 而对于客户端而言，SpringProxyInvokeHandler 会将默认连接的channel和客户端信息 放入senderThreadLocal
 * 并通过 方法provider 获取最新channel的方法，为没有channel的线程提供channel
 */
@Setter
public class RemoteInvoke {
    private boolean useDefaultProvider = false;
    private Supplier<ProxySender> provider;
    private final ThreadLocal<ProxySender> senderThreadLocal = new ThreadLocal<>();
    private final static RemoteInvoke instance = new RemoteInvoke();

    public static RemoteInvoke getInstance() {
        return instance;
    }

    public static RemoteInvoke getInstance(Sender sender, Channel channel) {
        if (channel != null) {
            instance.setThreadLocalSender(ProxySender.builder().sender(sender).channel(channel).build());
        }
        return instance;
    }

    public static RemoteInvoke getInstance(ProxySender channel) {
        if (channel != null) {
            instance.setThreadLocalSender(channel);
        }
        return instance;
    }


    public ProxySender getSender() {
        ProxySender proxySender = senderThreadLocal.get();
        //使用默认的ProxySender
        if (proxySender == null && useDefaultProvider) {
            proxySender = provider.get();
            setThreadLocalSender(proxySender);
        }
        return proxySender;
    }

    public void setDefaultSender(Sender sender, Channel channel) {
        this.setThreadLocalSender(ProxySender.builder().sender(sender).channel(channel).build());
    }

    public void setThreadLocalSender(ProxySender channel) {
        this.senderThreadLocal.set(channel);
    }

    //生成新的动态代理，代理方法为:RpcClientProxyHandler
    public <T> T generateDynamicProxy(Class<T> clazz) {
        return (T) JdkDynamicProxyUtil.proxyInvoke(clazz, new RpcClientProxyHandler());
    }

    public <T> T getBean(Class<T> clazz) {
        return SpringUtil.getBean(clazz);
    }

}
