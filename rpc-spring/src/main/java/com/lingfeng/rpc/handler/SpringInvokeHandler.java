package com.lingfeng.rpc.handler;

import com.lingfeng.rpc.client.handler.AbsClientHandler;
import com.lingfeng.rpc.client.nettyclient.NettyClient;
import com.lingfeng.rpc.frame.SafeFrame;
import com.lingfeng.rpc.invoke.ProxySender;
import com.lingfeng.rpc.invoke.RemoteInvoke;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;


@Slf4j
//@Component
//@ChannelHandler.Sharable
/**
 * 用于注册 动态代理的处理器 如果要使用@RpcHandler 需要将此处理器注册
 */
public class SpringInvokeHandler extends AbsClientHandler<SafeFrame<?>> {


    //@PostConstruct
    public SpringInvokeHandler() {

        log.info("SpringInvokeHandler init ");
        NettyClient client = getClient();
        log.info("client={}", client);
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        NettyClient client = this.getClient();
        client.defaultChannel(ctx.channel());
        super.channelRegistered(ctx);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        //为反射执行器注册默认的
        RemoteInvoke instance = RemoteInvoke.getInstance();
        //允许使用默认的channel进行通信
        instance.setUseDefaultProvider(true);
        instance.setProvider(() -> {
            //如果当前线程不能获取channel或者threadLocal 中的channel已经过期，可以通过这个回调方法获取最新的channel
            NettyClient client = getClient();
            Channel defaultChannel = client.getDefaultChannel();
            return ProxySender.builder().sender(client).channel(defaultChannel).build();
        });
        instance.setDefaultSender(getClient(), ctx.channel());
        super.channelActive(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("[netty client id: {}] exceptionCaught 客户端 error= {}", getClientId(), cause.getMessage(), cause);
        super.exceptionCaught(ctx, cause);
    }


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, SafeFrame<?> msg) throws Exception {
        ctx.fireChannelRead(msg);
    }
}
