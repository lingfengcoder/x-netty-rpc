package com.lingfeng.rpc.invoke;

import com.lingfeng.rpc.data.RpcInvokeFrame;

/**
 * @Auther: wz
 * @Date: 2022/6/20 15:02
 * @Description: 回调坐标
 */
public class CallbackPosition {

    //异步回调坐标
    private static final ThreadLocal<RpcInvokeFrame> asyncCallbackPositionThreadLocal = new ThreadLocal<>();

    //设置回调坐标
    public static void setCallbackPosition(RpcInvokeFrame callbackPosition) {
        asyncCallbackPositionThreadLocal.set(callbackPosition);
    }

    //获取回调坐标
    public static RpcInvokeFrame getCallbackPosition() {
        return asyncCallbackPositionThreadLocal.get();
    }

    //清除回调坐标
    public static void removeCallbackPosition() {
        asyncCallbackPositionThreadLocal.remove();
    }
}
