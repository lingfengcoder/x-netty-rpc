package com.lingfeng.rpc.handler;

import com.lingfeng.rpc.base.Sender;
import com.lingfeng.rpc.constant.Cmd;
import com.lingfeng.rpc.data.RpcInvokeFrame;
import com.lingfeng.rpc.data.RpcRespFrame;
import com.lingfeng.rpc.data.Snotify;
import com.lingfeng.rpc.frame.SafeFrame;
import com.lingfeng.rpc.invoke.RemoteInvoke;
import com.lingfeng.rpc.invoke.RpcInvokeProxy;
import com.lingfeng.rpc.util.relfect.ReflectUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.lang.reflect.Method;

/**
 * @Auther: wz
 * @Date: 2022/6/20 13:46
 * @Description:
 */
@Slf4j
public class RpcConsumer {
    public static Object waitFor(String seq, Object lock, long timeout) throws InterruptedException {
        RpcStore.offer(seq, new Snotify().setSeq(seq).setLock(lock).setTimeout(timeout));
        synchronized (lock) {
            if (timeout > 0) {
                lock.wait(timeout);
            } else {
                lock.wait();
            }
        }
        //wake up
        Snotify snotify = RpcStore.get(seq);
        //clear resp-lock
        RpcStore.remove(seq);
        //do callback
        return snotify.getData();
    }

    public static Object waitFor(String seq, Object lock) throws InterruptedException {
        return waitFor(seq, lock, 0);
    }

    public static void rpcRequestHandler(ChannelHandlerContext ctx, SafeFrame<Object> data, ThreadPoolTaskExecutor executeThreadPool, Sender sender) {
        log.info("receive RpcInvokeFrame={}", data);
        //强制类型转换
        RpcInvokeFrame frame = (RpcInvokeFrame) data.getContent();
        //异步回调的坐标
        RpcInvokeFrame retPosition = frame.getRetPosition();
        String seq = frame.getSeq();
        Channel channel = ctx.channel();
        //是否是同步模式或异步模式
        boolean sync = frame.isSync();
        //线程池执行
        executeThreadPool.execute(() -> RpcInvokeProxy.invoke(channel, result -> {
            if (sync) {
                //同步处理
                syncHandler(result, seq, sender, channel);
            } else {
                //异步处理
                asyncHandler(result, retPosition, sender, channel);
            }
        }, frame));
    }

    //同步模式处理
    private static void syncHandler(Object result, String seq, Sender sender, Channel channel) {
        String channelId = channel.id().asLongText();
        log.info("channel={}", channelId);
        //rpc标准返回数据帧
        RpcRespFrame<Object> respFrame = new RpcRespFrame<>();
        //设置同步标识
        respFrame.setSync(true);
        respFrame.setData(result);
        respFrame.setSeq(seq);
        respFrame.setClientId(channelId);
        sender.writeAndFlush(channel, respFrame, Cmd.RPC_RESP);
    }

    //异步模式处理
    private static void asyncHandler(Object result, RpcInvokeFrame position, Sender sender, Channel channel) {
        if (position != null) {
            //如果回调的坐标不为空的话
            String beanName = position.getBeanName();
            RemoteInvoke invoke = RemoteInvoke.getInstance(sender, channel);
            //获取代理类
            Object clientApi = invoke.getBean(beanName);
            //通过反射执行,此处或触发代理类的增强方法，并将数据发送出去
            ReflectUtil.invokeMethodByName(clientApi, position.getMethodName(), new Object[]{result});
        }
    }

}
