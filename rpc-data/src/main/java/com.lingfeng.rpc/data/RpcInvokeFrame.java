package com.lingfeng.rpc.data;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Map;

@Setter
@Getter
@ToString
public class RpcInvokeFrame implements Serializable {
    //消息号
    private String seq;
    //目标bean
    private String beanName;
    //目标方法
    private String methodName;
    //参数类型
    private Class<?>[] parameterTypes;
    //参数集合
    private Object[] arguments;
    //附带数据
    private Map<String, String> attachments;
    //返回数据的坐标 (处理器处理完毕数据后，将返回的消息返回到这个坐标上)
    private RpcInvokeFrame retPosition;
    //是否是同步
    private boolean sync;
}
