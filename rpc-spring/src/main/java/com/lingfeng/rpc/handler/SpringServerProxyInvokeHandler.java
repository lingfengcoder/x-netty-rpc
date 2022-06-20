package com.lingfeng.rpc.handler;

import com.lingfeng.rpc.constant.Cmd;
import com.lingfeng.rpc.data.Frame;
import com.lingfeng.rpc.data.RpcInvokeFrame;
import com.lingfeng.rpc.data.RpcRespFrame;
import com.lingfeng.rpc.data.Snotify;
import com.lingfeng.rpc.frame.SafeFrame;
import com.lingfeng.rpc.invoke.RpcInvokeProxy;
import com.lingfeng.rpc.server.handler.AbsServerHandler;
import com.lingfeng.rpc.server.nettyserver.NettyServer;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;


/**
 * 用于注册 动态代理的处理器 如果要使用@RpcHandler 需要将此处理器注册
 */
@Slf4j
@ChannelHandler.Sharable
public class SpringServerProxyInvokeHandler extends AbsServerHandler<SafeFrame<Object>> {

    private ThreadPoolTaskExecutor executeThreadPool;

    public void setExecuteThreadPool(ThreadPoolTaskExecutor executeThreadPool) {
        this.executeThreadPool = executeThreadPool;
    }

    public SpringServerProxyInvokeHandler(ThreadPoolTaskExecutor executeThreadPool) {
        this.setExecuteThreadPool(executeThreadPool);
        log.info("SpringServerProxyInvokeHandler init ");
    }


    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        super.channelRegistered(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("[netty client id: {}] exceptionCaught 客户端ctx error= {}", ctx.channel().id().asLongText(), cause.getMessage(), cause);
        super.exceptionCaught(ctx, cause);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, SafeFrame<Object> data) {
        byte cmd = data.getCmd();
        if (cmd == Cmd.TEST.code()) {
            log.info("channelRead0 receive string msg={}", data.getContent());
            return;
        }
        //处理RPC请求
        if (cmd == Cmd.RPC_REQ.code()) {
            NettyServer server = getServer();
            RpcConsumer.rpcRequestHandler(ctx, data, executeThreadPool, server);
        } else if (cmd == Cmd.RPC_RESP.code()) {
            RpcProvider.rpcResponseHandler(ctx, data);
            //转交消息
        } else ctx.fireChannelRead(data);
    }

}
