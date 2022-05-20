package com.lingfeng.rpc.server.nettyserver;

import com.lingfeng.rpc.constant.Cmd;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;

import java.io.Serializable;
import java.util.Collection;

public interface NettyServer {
    //服务器状态
    int state();

    //开启服务器
    void start();

    //重启服务器
    void restart();

    //停止服务器
    void stop();

    //获取服务器id
    long getServerId();

    //发送消息
    <M extends Serializable> void writeAndFlush(Channel channel, M msg, Cmd type);

    <M extends Serializable> void writeAndFlush(ChannelHandlerContext channel, M msg, Cmd type);

    //添加客户端channel
    void addChannel(String clientId, Channel channel);

    //关闭指定clientId的channel
    void closeChannel(String clientId);

    //获取所有的客户端channel
    Collection<Channel> allChannels();

    //获取指定clientId的客户端channel
    Channel findChanel(String clientId);

    //打印所有channel信息
    void showChannels();
}
