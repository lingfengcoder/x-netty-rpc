package com.lingfeng.rpc.handler;

import com.lingfeng.rpc.data.Snotify;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @Auther: wz
 * @Date: 2022/6/20 12:57
 * @Description:
 */
@Slf4j
public class PSTest {

    public static void main(String[] args) throws InterruptedException {
        int x = 10000;
        for (int i = 0; i < x; i++) {
            int finalI = i;
            new Thread(() -> {
                Object lock = new Object();
                String seq = "seq-" + finalI;
                try {
                    Object resp = waitFor(seq, lock);
                    log.info("任务:{}获得消息:{}", seq, resp);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }).start();
        }
        Thread.sleep(3000);

        Thread provider1 = new Thread(() -> {
            for (int i = 0; i < x/2; i++) {
                Snotify snotify = respMap.get("seq-" + i);
                Object lock = snotify.getLock();
                synchronized (lock) {
                    Thread thread = Thread.currentThread();
                    snotify.setData("provider: thread-1 resp data :" + i);
                    lock.notify();
                }
            }
        });
        Thread provider2 = new Thread(() -> {
            for (int i = x/2; i < x; i++) {
                Snotify snotify = respMap.get("seq-" + i);
                Object lock = snotify.getLock();
                synchronized (lock) {
                    Thread thread = Thread.currentThread();
                    snotify.setData("provider: thread-2 resp data :" + i);
                    lock.notify();
                }
            }
        });
        provider1.start();
        provider2.start();

    }

    private final static ConcurrentHashMap<String, Snotify> respMap = new ConcurrentHashMap<>();

    public static Object waitFor(String seq, Object lock, long timeout) throws InterruptedException {
        respMap.put(seq, new Snotify().setSeq(seq).setLock(lock));
        synchronized (lock) {
            if (timeout > 0) {
                lock.wait(timeout);
            } else {
                lock.wait();
            }
        }
        //wake up
        Snotify snotify = respMap.get(seq);
        //clear resp-lock
        respMap.remove(seq);
        //do callback
        return snotify.getData();
    }

    public static Object waitFor(String seq, Object lock) throws InterruptedException {
        return waitFor(seq, lock, 0);
    }
}
