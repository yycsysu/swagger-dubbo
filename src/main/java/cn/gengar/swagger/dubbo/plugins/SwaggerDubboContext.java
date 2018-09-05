package cn.gengar.swagger.dubbo.plugins;

import com.alibaba.dubbo.config.spring.ReferenceBean;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author gengar yu
 */

public class SwaggerDubboContext {

    private static final Map<String, DubboMethod> methodCache = new ConcurrentHashMap<>();
    private static final Map<String, ReferenceBean> referenceBeanCache = new ConcurrentHashMap<>();

    private SwaggerDubboContext() {}

    public static void clearMethodCache() {
        methodCache.clear();
    }

    public static void clearReferenceBeanCache() {
        referenceBeanCache.clear();
    }

    public static void putMethod(String key, DubboMethod dubboMethod) {
        methodCache.put(key, dubboMethod);
    }

    public static Collection<DubboMethod> getMethods() {
        return Collections.unmodifiableCollection(methodCache.values());
    }

    public static void bindMethod(String source, String target) {
        methodCache.put(target, methodCache.get(source));
    }

    public static DubboMethod getMethod(String key) {
        return methodCache.get(key);
    }

    public static ReferenceBean getReferenceBean(String key) {
        return referenceBeanCache.get(key);
    }

    public static void putReferenceBean(String key, ReferenceBean referenceBean) {
        referenceBeanCache.put(key, referenceBean);
    }
}
