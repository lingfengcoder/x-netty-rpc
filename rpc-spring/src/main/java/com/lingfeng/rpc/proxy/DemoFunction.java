package com.lingfeng.rpc.proxy;

import com.lingfeng.rpc.data.Frame;

import java.util.Map;

/**
 * @Auther: wz
 * @Date: 2022/6/16 09:35
 * @Description:
 */
public interface DemoFunction {
    public void bbq(Map<String,String>map, Frame frame);
}
