package com.lingfeng.rpc.data;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * @Auther: wz
 * @Date: 2022/6/20 12:36
 * @Description:
 */
@Setter
@Getter
@Accessors(chain = true)
public class Snotify {
    private String seq;
    private Object lock;
    private Object data;
    private long timeout;
}
