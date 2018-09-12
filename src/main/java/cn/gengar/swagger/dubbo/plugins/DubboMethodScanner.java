package cn.gengar.swagger.dubbo.plugins;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.config.model.ApplicationModel;
import com.alibaba.dubbo.config.model.ProviderMethodModel;
import com.alibaba.dubbo.config.model.ProviderModel;
import com.alibaba.dubbo.rpc.Exporter;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.protocol.dubbo.DubboProtocol;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author gengar yu
 */

@Component
public class DubboMethodScanner {

    @Value("${swagger.dubbo.context:}")
    private String context;

    public List<DubboMethod> scan() {

        // from already exported
        List<DubboMethod> methods = scanProvider();

        return methods;
    }

    private List<DubboMethod> scanProvider() {
        synchronized (this) {
            SwaggerDubboContext.clearMethodCache();

            List<DubboMethod> dubboMethods = new ArrayList<>();
            for (ProviderModel model : ApplicationModel.allProviderModels()) {
                List<URL> urls = model.getMetadata().getExportedUrls();
                for (URL url : urls) {
                    Map<String, List<ProviderMethodModel>> byMethodName =
                            model.getAllMethods().stream()
                                    .collect(Collectors.groupingBy(ProviderMethodModel::getMethodName));

                    for (List<ProviderMethodModel> methods : byMethodName.values()) {
                        boolean hasOverload = methods.size() > 1;
                        int order = 0;
                        for (ProviderMethodModel method : methods) {
                            DubboMethod dubboMethod = new DubboMethod(model, url, method, order++, hasOverload, context);
                            dubboMethods.add(dubboMethod);

                            SwaggerDubboContext.putMethod(dubboMethod.getMethodKey(), dubboMethod);
                        }
                    }
                }
            }

            return dubboMethods;
        }
    }

    @Deprecated
    private List<DubboMethod> scanExporters() {
        synchronized (this) {
            SwaggerDubboContext.clearMethodCache();

            List<DubboMethod> dubboMethods = new ArrayList<>();
            for (Exporter<?> exporter : DubboProtocol.getDubboProtocol().getExporters()) {
                Invoker<?> invoker = exporter.getInvoker();
                URL url = invoker.getUrl();
                Class<?> interfaceClass = invoker.getInterface();
                Object ref = findReferObject(invoker);

                Map<String, List<Method>> byMethodName =
                        Stream.of(interfaceClass.getDeclaredMethods())
                                .collect(Collectors.groupingBy(Method::getName));

                for (List<Method> methods : byMethodName.values()) {
                    boolean hasOverload = methods.size() > 1;
                    int order = 0;
                    for (Method method : methods) {
                        DubboMethod dubboMethod = new DubboMethod(ref, url, method, order++, hasOverload, context);
                        dubboMethods.add(dubboMethod);

                        SwaggerDubboContext.putMethod(dubboMethod.getMethodKey(), dubboMethod);
                    }
                }
            }

            return dubboMethods;
        }
    }

    private Object findReferObject(Object proxy) {
        try {
            Object current = proxy;
            Object next;
            while ((next = findLikelyObject(current, "invoker")) != null) {
                current = next;
            }

            Object target = findLikelyObject(current, "proxy");
            if (target != null) {
                return target;
            } else {
                return null;
            }

        } catch (Exception e) {
            return null;
        }
    }

    private Object findLikelyObject(Object invoker, String likely) throws IllegalAccessException {
        Class classOfInvoker = invoker.getClass();
        Field[] fields = getAllFields(classOfInvoker);
        Field field = findLikelyField(fields, likely);

        if (field != null) {
            field.setAccessible(true);
            return field.get(invoker);
        } else {
            return null;
        }
    }

    private Field findLikelyField(Field[] fields, String likely) {
        for (Field field : fields) {
            if (field.getName().contains(likely)) {
                return field;
            }
        }
        return null;
    }

    private Field[] getAllFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();

        Class<?> current = clazz;
        while (current != null) {
            Collections.addAll(fields, current.getDeclaredFields());
            current = current.getSuperclass();
        }

        return fields.toArray(new Field[fields.size()]);
    }
}
