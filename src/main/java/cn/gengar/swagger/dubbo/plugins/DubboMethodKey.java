package cn.gengar.swagger.dubbo.plugins;

import java.util.Objects;

/**
 * @author gengar yu
 */

public class DubboMethodKey {

    private final String serviceKey;
    private final String methodName;
    private final int order;
    private final String nickName;

    private DubboMethodKey(DubboMethod method) {
        this.serviceKey = method.getServiceKey();
        this.methodName = method.getMethod().getName();
        this.order = method.getOrder();
        this.nickName = "";
    }

    private DubboMethodKey(DubboMethod method, String nickName) {
        this.serviceKey = method.getServiceKey();
        this.methodName = method.getMethod().getName();
        this.order = method.getOrder();
        this.nickName = nickName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DubboMethodKey that = (DubboMethodKey) o;
        return order == that.order &&
                Objects.equals(serviceKey, that.serviceKey) &&
                Objects.equals(methodName, that.methodName) &&
                Objects.equals(nickName, that.nickName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(serviceKey, methodName, order, nickName);
    }

    @Override
    public String toString() {
        return "DubboMethodKey{" +
                "serviceKey='" + serviceKey + '\'' +
                ", methodName='" + methodName + '\'' +
                ", order=" + order +
                ", nickName='" + nickName + '\'' +
                '}';
    }
}
