package com.lingfeng.rpc.serial;


import com.lingfeng.rpc.constant.SerialType;
import com.lingfeng.rpc.util.GsonTool;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;

/**
 * <p>
 * </p>
 *
 * @author chenchaobiao
 * @date 2022/5/7 14:53
 * @since 1.0.0
 */
@Slf4j
public class JsonSerializer implements ISerializer {
    @Override
    public <T> byte[] serialize(T obj) {
        return GsonTool.toJson(obj).getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public <T> T deserialize(byte[] data, Class<T> clazz) {
        String str = new String(data);
        return GsonTool.fromJson(str, clazz);
    }

    @Override
    public byte getType() {
        return SerialType.JSON_SERIAL.code();
    }
}