package com.lingfeng.rpc.invoke;

import java.util.Arrays;
import java.util.Objects;

public class RpcClientSpecification   {
    private String name;
    private Class<?>[] configuration;

    RpcClientSpecification() {
    }

    RpcClientSpecification(String name, Class<?>[] configuration) {
        this.name = name;
        this.configuration = configuration;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Class<?>[] getConfiguration() {
        return this.configuration;
    }

    public void setConfiguration(Class<?>[] configuration) {
        this.configuration = configuration;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o != null && this.getClass() == o.getClass()) {
            RpcClientSpecification that = (RpcClientSpecification)o;
            return Objects.equals(this.name, that.name) && Arrays.equals(this.configuration, that.configuration);
        } else {
            return false;
        }
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.name, this.configuration});
    }

    public String toString() {
        return "FeignClientSpecification{" + "name='" + this.name + "', " + "configuration=" + Arrays.toString(this.configuration) + "}";
    }
}
