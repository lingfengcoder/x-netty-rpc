package com.lingfeng.rpc.handler;

import com.lingfeng.rpc.data.RpcRespFrame;
import com.lingfeng.rpc.data.Snotify;
import com.lingfeng.rpc.frame.SafeFrame;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

/**
 * @Auther: wz
 * @Date: 2022/6/20 13:42
 * @Description:
 */
@Slf4j
public class RpcProvider {


    public static void rpcResponseHandler(ChannelHandlerContext ctx, SafeFrame<Object> data) {
        if (data != null) {
            RpcRespFrame<Object> content = (RpcRespFrame) data.getContent();
            //如果是异步处理的数据直接警告，并放弃处理
            if (!content.isSync()) {
                log.warn("消息：{} 是异步消息！请通过异步模式处理", data);
                return;
            }
            //获取对应的
            Snotify snotify = RpcStore.get(content.getSeq());
            if (snotify != null) {
                Object lock = snotify.getLock();
                if (lock != null) {
                    synchronized (lock) {
                        //设置数据
                        snotify.setData(content.getData());
                        //唤醒线程
                        lock.notify();
                    }
                }
            } else {
                log.info("seq={} 没有找到锁 返回的数据：{}", content.getSeq(), data);
            }
        } else {
            String channelId = ctx.channel().id().asLongText();
            log.warn("channelId={} 获取的数据为null", channelId);
        }
    }
}
