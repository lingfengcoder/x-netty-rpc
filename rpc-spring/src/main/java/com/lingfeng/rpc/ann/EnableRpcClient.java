package com.lingfeng.rpc.ann;

import com.lingfeng.rpc.invoke.RpcDemoRegister;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * @Auther: wz
 * @Date: 2022/6/16 13:40
 * @Description: 是否启用rpcClient
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Documented
@Import({RpcDemoRegister.class})
public @interface EnableRpcClient {
    String[] value() default {};

    String[] basePackages() default {};

    Class<?>[] basePackageClasses() default {};

    Class<?>[] defaultConfiguration() default {};

    Class<?>[] clients() default {};
}
