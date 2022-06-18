package com.lingfeng.rpc.base;

import com.lingfeng.rpc.constant.Cmd;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;

import java.io.Serializable;

public interface Sender {
//    <M extends Serializable> void writeAndFlush(M msg, Cmd type);

    <M extends Serializable> void writeAndFlush(Channel channel, M msg, Cmd type);

    <M extends Serializable> void writeAndFlush(ChannelHandlerContext channel, M msg, Cmd type);
}
