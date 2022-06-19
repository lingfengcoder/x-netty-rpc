package com.lingfeng.rpc.proxy;

import com.lingfeng.rpc.base.Sender;
import com.lingfeng.rpc.constant.Cmd;
import io.netty.channel.Channel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;

@Setter
@Getter
@Builder
@Slf4j
public class ProxySender {
    private Sender sender;
    private Channel channel;

    public <M extends Serializable> void writeAndFlush(M msg) {
        sender.writeAndFlush(channel, msg, Cmd.RPC_REQ);
    }
}
