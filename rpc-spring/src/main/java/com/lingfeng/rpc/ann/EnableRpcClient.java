package com.lingfeng.rpc.ann;

import com.lingfeng.rpc.proxy.RpcClientRegister;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * @Auther: wz
 * @Date: 2022/6/16 13:40
 * @Description: 是否启用rpcClient
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Documented
@Import({RpcClientRegister.class})
public @interface EnableRpcClient {
    String[] value() default {};

    String[] basePackages() default {};

    Class<?>[] basePackageClasses() default {};

    Class<?>[] defaultConfiguration() default {};

    Class<?>[] clients() default {};
}
