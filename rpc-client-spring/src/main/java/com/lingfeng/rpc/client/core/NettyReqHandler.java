package com.lingfeng.rpc.client.core;

import cn.hutool.extra.spring.SpringUtil;
import com.lingfeng.rpc.client.handler.AbsClientHandler;
import com.lingfeng.rpc.constant.Cmd;
import com.lingfeng.rpc.data.Frame;
import com.lingfeng.rpc.data.RpcInvokeFrame;
import com.lingfeng.rpc.frame.SafeFrame;
import com.lingfeng.rpc.invoke.RpcInvokeProxy;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Slf4j
public class NettyReqHandler extends AbsClientHandler<SafeFrame<RpcInvokeFrame>> {

    private volatile ThreadPoolTaskExecutor executor;

    //线程池处理
    public ThreadPoolTaskExecutor getExecutor() {
        if (executor == null) {
            synchronized (this) {
                if (executor == null) {
                    executor = SpringUtil.getBean("nettyClientThreadPool");
                }
            }
        }
        return executor;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, SafeFrame<RpcInvokeFrame> data) throws Exception {
        byte cmd = data.getCmd();
        if (cmd == Cmd.REQUEST.code()) {
            RpcInvokeFrame frame = data.getContent();
            String methodName = frame.getMethodName();
            //使用线程池处理任务
            getExecutor().execute(() -> {
                //代理执行方法
                RpcInvokeProxy.invoke(ctx.channel(), ret -> {
                    log.info("返回数据={}", ret);
                    //返回数据
//                    Frame<Object> resp = new Frame<>();
//                    resp.setData(ret);
//                    writeAndFlush(ctx.channel(), resp, Cmd.RESPONSE);
                }, frame);
            });

        } else {
            // ctx.fireChannelRead(data);
        }
    }
}
