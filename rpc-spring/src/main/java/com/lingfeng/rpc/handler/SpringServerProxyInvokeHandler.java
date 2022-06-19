package com.lingfeng.rpc.handler;

import com.lingfeng.rpc.constant.Cmd;
import com.lingfeng.rpc.data.Frame;
import com.lingfeng.rpc.data.RpcInvokeFrame;
import com.lingfeng.rpc.frame.SafeFrame;
import com.lingfeng.rpc.invoke.RpcInvokeProxy;
import com.lingfeng.rpc.server.handler.AbsServerHandler;
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
public class SpringServerProxyInvokeHandler extends AbsServerHandler<SafeFrame<RpcInvokeFrame>> {

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
    protected void channelRead0(ChannelHandlerContext ctx, SafeFrame<RpcInvokeFrame> data) throws Exception {

        byte cmd = data.getCmd();
        if (cmd == Cmd.TEST.code()) {
            log.info("channelRead0 receive string msg={}", data.getContent());
            return;
        }
        // request 主要处理client的请求，比如 客户端主动请求获取最新的配置 客户端注册
        //处理RPC请求
        if (cmd == Cmd.RPC_REQ.code()) {
            log.info("channelRead0 receive RpcInvokeFrame={}", data);
            RpcInvokeFrame frame = data.getContent();
            Channel channel = ctx.channel();
            //线程池执行
            executeThreadPool.execute(() -> {
                String channelId = channel.id().asLongText();
                log.info("channel={}", channelId);
                RpcInvokeProxy.invoke(channel, ret -> {
                    // FinishNotify finishNotify;
                    // finishNotify.finish(666,"666");
                    //返回数据
                    Frame<Object> fame = new Frame<>();
                    fame.setData(ret);
                    fame.setClientId(channelId);
                    writeAndFlush(channel, fame, Cmd.RPC_RESP);
                }, frame);
            });
            //转交消息
        } else ctx.fireChannelRead(data);
    }


}
