package com.lingfeng.rpc.client.nettyclient;

import com.lingfeng.rpc.client.handler.*;
import com.lingfeng.rpc.coder.Coder;
import com.lingfeng.rpc.coder.CoderFactory;
import com.lingfeng.rpc.coder.safe.SafeCoder;
import com.lingfeng.rpc.demo.DemoClientHandler;
import com.lingfeng.rpc.model.Address;
import com.lingfeng.rpc.server.nettyserver.BizNettyServer;
import io.netty.channel.ChannelHandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * @Author: wz
 * @Date: 2022/5/12 11:20
 * @Description:
 */
public class NettyClientFactory {

    public static <T> T generateClient(Address address, Class<T> clazz) {
        if (clazz.equals(BizNettyClient.class)) {
            return (T) buildBizNettyClient(address);
        }
        return null;
    }

    private static BizNettyClient buildBizNettyClient(Address address) {
        return buildBizNettyClient(address, () -> Collections.singletonList((new MyClientHandler())));
    }

    public static <T> BizNettyClient buildBizNettyClient(Address address, Supplier<List<ChannelHandler>> func) {
        BizNettyClient client = new BizNettyClient();
        client.setAddress(address);
        //通过配置的方式，可以保证每次重启都获取新的handler对象 ,从而避免了@Sharable
        client.config(_client -> {
            //coder 不允许共享需要单独添加
            //自定义传输协议
            CoderFactory coderFactory = CoderFactory.getInstance();
            Coder generate = coderFactory.generate(SafeCoder.class);
            _client
                    .addHandler(generate.type())
                    .addHandler(generate.decode())
                    .addHandler(generate.encode())
                    //空闲处理器
                    .addHandler(IdleHandler.getIdleHandler())
                    //心跳处理器
                    .addHandler(new HeartHandler())// HeartHandler.NAME
                    //监听器
                    .addListener(new ReConnectFutureListener());
            //添加业务处理器
            if (func != null) {
                List<ChannelHandler> handlers = func.get();
                for (ChannelHandler handler : handlers) {
                    _client.addHandler(handler);
                }
            }
        });
        return client;
    }

    public static <T> BizNettyClient buildBizNettyClient(Address address, ChannelHandler... channelHandlers) {
        BizNettyClient client = new BizNettyClient();
        client.setAddress(address);
        //通过配置的方式，可以保证每次重启都获取新的handler对象 ,从而避免了@Sharable
        client.config(_client -> {
            //coder 不允许共享需要单独添加
            //自定义传输协议
            CoderFactory coderFactory = CoderFactory.getInstance();
            Coder generate = coderFactory.generate(SafeCoder.class);
            _client
                    .addHandler(generate.type())
                    .addHandler(generate.decode())
                    .addHandler(generate.encode())
                    //空闲处理器
                    .addHandler(IdleHandler.getIdleHandler())
                    //心跳处理器
                    .addHandler(new HeartHandler())// HeartHandler.NAME
                    //监听器
                    .addListener(new ReConnectFutureListener());
            //添加业务处理器
            if (channelHandlers != null) {
                for (ChannelHandler handler : channelHandlers) {
                    _client.addHandler(handler);
                }
            }
        });
        return client;
    }


    public static <T> BizNettyClient buildSimpleClient(Address address, Supplier<List<AbsClientHandler<T>>> func) {
        BizNettyClient client = new BizNettyClient();
        client.setAddress(address);
        //通过配置的方式，可以保证每次重启都获取新的handler对象 ,从而避免了@Sharable
        client.config(_client -> {

            //自定义传输协议
            _client
                    .addListener(new ReConnectFutureListener());
            //添加业务处理器
            if (func != null) {
                List<AbsClientHandler<T>> handlers = func.get();
                for (AbsClientHandler<T> handler : handlers) {
                    _client.addHandler(handler);
                }
            }
        });
        return client;
    }
}
