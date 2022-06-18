package com.lingfeng.rpc.proxy;

import com.lingfeng.rpc.data.Frame;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

/**
 * @Auther: wz
 * @Date: 2022/6/16 09:34
 * @Description:
 */
@Setter
@Slf4j
public class JdkDynamicProxyUtil {

    public static void main(String[] args) {

        DemoFunction newProxyInstance = proxyInvoke(DemoFunction.class, (proxy, method, args1) -> {
            log.info("proxy={} method={} args={}", proxy, method, args1);
            return null;
        });

        Map<String, String> map = new HashMap<>();
        map.put("1", "111");
        map.put("2", "222");
        map.put("3", "333");
        Frame frame = new Frame();
        frame.setTarget("bbq target");
        newProxyInstance.bbq(map, frame);
        //动态代理
    }

    public static <T> T proxyInvoke(Class<T> clazz, InvocationHandler handler) {
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz}, handler);
    }

    public static <T> T proxyInvoke(InvocationHandler handler, ClassLoader loader, Class<T>... classes) {
        return (T) Proxy.newProxyInstance(loader, classes, handler);
    }

}
