package com.lingfeng.rpc.handler;

import com.lingfeng.rpc.data.Snotify;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @Auther: wz
 * @Date: 2022/6/20 13:46
 * @Description:
 */
public class RpcStore {
    private final static ConcurrentHashMap<String, Snotify> queue = new ConcurrentHashMap<>();

    public static ConcurrentHashMap<String, Snotify> queue() {
        return queue;
    }

    public static Snotify get(String req) {
        return queue.get(req);
    }

    public static void offer(String req, Snotify snotify) {
        queue.put(req, snotify);
    }

    public static void remove(String req) {
        queue.remove(req);
    }
}
