package com.lingfeng.rpc.handler;

import com.lingfeng.rpc.client.handler.AbsClientHandler;
import com.lingfeng.rpc.client.nettyclient.NettyClient;
import com.lingfeng.rpc.constant.Cmd;
import com.lingfeng.rpc.data.Frame;
import com.lingfeng.rpc.data.RpcInvokeFrame;
import com.lingfeng.rpc.frame.SafeFrame;
import com.lingfeng.rpc.invoke.RpcInvokeProxy;
import com.lingfeng.rpc.proxy.ProxySender;
import com.lingfeng.rpc.invoke.RemoteInvoke;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * 用于注册 动态代理的处理器 如果要使用@RpcHandler 需要将此处理器注册
 */
@Slf4j
@ChannelHandler.Sharable
public class SpringClientProxyInvokeHandler extends AbsClientHandler<SafeFrame<Object>> {

    private ThreadPoolTaskExecutor executeThreadPool;

    public void setExecuteThreadPool(ThreadPoolTaskExecutor executeThreadPool) {
        this.executeThreadPool = executeThreadPool;
    }

    //@PostConstruct
    public SpringClientProxyInvokeHandler(ThreadPoolTaskExecutor executeThreadPool) {
        log.info("SpringInvokeHandler init ");
        NettyClient client = getClient();
        log.info("client={}", client);
        this.setExecuteThreadPool(executeThreadPool);
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        NettyClient client = this.getClient();
        client.defaultChannel(ctx.channel());
        super.channelRegistered(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("[netty client id: {}] exceptionCaught 客户端 error= {}", getClientId(), cause.getMessage(), cause);
        super.exceptionCaught(ctx, cause);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, SafeFrame<Object> data) {
        byte cmd = data.getCmd();
        if (cmd == Cmd.TEST.code()) {
            log.debug("[ClientProxyInvokeHandler]channelRead0 receive string msg={}", data.getContent());
            return;
        }
        //处理rpc请求
        if (cmd == Cmd.RPC_REQ.code()) {
            NettyClient client = getClient();
            RpcConsumer.rpcRequestHandler(ctx, data, executeThreadPool, client);
        } else if (cmd == Cmd.RPC_RESP.code()) {
            RpcProvider.rpcResponseHandler(ctx, data);
            //转交消息
        } else ctx.fireChannelRead(data);
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
}
