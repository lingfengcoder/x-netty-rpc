package com.lingfeng.rpc.data;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

@Setter
@Getter
@ToString
public class RpcRespFrame<T> implements Serializable {
    //消息序列号
    private String seq;
    //携带数据
    private T data;
    //消息所属客户端
    private String clientId;
    //是否是同步处理 true:是同步
    private boolean sync;
}