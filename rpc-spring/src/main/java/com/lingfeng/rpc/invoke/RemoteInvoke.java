package com.lingfeng.rpc.invoke;

import cn.hutool.extra.spring.SpringUtil;
import com.lingfeng.rpc.base.Sender;
import com.lingfeng.rpc.proxy.JdkDynamicProxyUtil;
import io.netty.channel.Channel;
import lombok.Setter;

import java.util.function.Consumer;
import java.util.function.Supplier;


/**
 * @Auther: wz
 * @Date: 2022/6/16 16:13
 * @Description:
 */
@Setter
public class RemoteInvoke {

    private boolean useDefaultProvider = false;
    private Supplier<ProxySender> provider;
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

    private final ThreadLocal<ProxySender> senderThreadLocal = new ThreadLocal<>();

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

    public <T> T getDynamicProxy(Class<T> clazz) {
        return SpringUtil.getBean(clazz);
    }

    public <T> T getDynamicProxy2(Class<T> clazz) {
        return (T) JdkDynamicProxyUtil.proxyInvoke(clazz, new RemoteProcess());
    }
//
//    public static <T> T getProxy(Class<T> clazz) {
//        RpcTargetRegister bean = SpringUtil.getBean(RpcTargetRegister.class);
//        return (T) bean.getDynamicProxyCache().get(clazz);
//    }

}
