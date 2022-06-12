package com.lingfeng.rpc.client.core;

import cn.hutool.core.thread.ThreadFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @Author: wz
 * @Date: 2022/5/16 11:10
 * @Description:
 */
@Configuration
public class ThreadConfig {
    @Bean
    //调度器发送线程池
    public ThreadPoolTaskExecutor nettyClientThreadPool() {
        ThreadFactory factory = ThreadFactoryBuilder.create()
                .setNamePrefix("##nettyClientThreadPool##").setDaemon(false)
                .build();
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setKeepAliveSeconds(60);
        executor.setThreadFactory(factory);
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(50);
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
        executor.setAllowCoreThreadTimeOut(true);
        executor.initialize();
        return executor;
    }

}
