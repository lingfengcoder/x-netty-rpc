package com.lingfeng.rpc.invoke;

import cn.hutool.extra.spring.SpringUtil;



/**
 * @Auther: wz
 * @Date: 2022/6/16 16:13
 * @Description:
 */
public class RemoteInvoke {

    private ThreadLocal<Object>

    public static <T> T getProxy(Class<T> clazz) {
        RpcClientRegister bean = SpringUtil.getBean(RpcClientRegister.class);
        return (T) bean.getDynamicProxyCache().get(clazz);
    }

}
