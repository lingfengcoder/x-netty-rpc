package com.lingfeng.rpc.ann;

import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Auther: wz
 * @Date: 2022/6/16 13:40
 * @Description:
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface RpcClient {
    String value() default "";

    String contextId() default "";

    @AliasFor("value")
    String name() default "";

    /**
     * @deprecated
     */
    @Deprecated
    String qualifier() default "";

    String[] qualifiers() default {};

    String url() default "";

    boolean decode404() default false;

    Class<?>[] configuration() default {};

    Class<?> fallback() default void.class;

    Class<?> fallbackFactory() default void.class;

    String path() default "";

    boolean primary() default true;
}