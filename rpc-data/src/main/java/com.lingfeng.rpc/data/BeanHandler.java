package com.lingfeng.rpc.data;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;

@Setter
@Getter
@ToString
public class BeanHandler {
    private String beanName;
    private HashMap<String, Method> method;
    private Object bean;
}
