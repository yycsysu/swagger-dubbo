package cn.gengar.swagger.dubbo.plugins;

import cn.gengar.swagger.dubbo.exception.SwaggerDubboException;
import cn.gengar.swagger.dubbo.utils.JsonUtils;
import com.alibaba.dubbo.config.spring.ReferenceBean;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

/**
 * @author gengar yu
 */

public class DubboMethodInvoker {

    private static final Logger LOG = LoggerFactory.getLogger(DubboMethodInvoker.class);

    public static Object invoke(String methodKey, String requestBody, Map<String, String[]> requestMap) {
        Preconditions.checkState(!Strings.isNullOrEmpty(methodKey));

        DubboMethod method = SwaggerDubboContext.getMethod(methodKey);
        ReferenceBean referenceBean = SwaggerDubboContext.getReferenceBean(method.getServiceKey());
        Object[] parameters = transformParameters(method, requestBody, requestMap);

        try {
            Object target = referenceBean.get();
            Method targetMethod = target.getClass().getMethod(method.getMethod().getName(), method.getMethod().getParameterTypes());
            return targetMethod.invoke(target, parameters);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            LOG.error("Invoke target method: {} failed. parameters {}", method.getMethodNameReadable(), parameters);
            throw new SwaggerDubboException(e.getMessage(), e);
        }
    }

    private static Object[] transformParameters(DubboMethod method, String requestBody, Map<String, String[]> requestMap) {
        if (method.isJsonType()) {
            Parameter[] parameters = method.getMethod().getParameters();

            if (parameters.length <= 0) {
                return new Object[0];
            }

            Parameter parameter = parameters[0];
            Object[] parameterInstances = new Object[1];

            try {
                Object parameterInstance = JsonUtils.readValueFromRealType(requestBody, parameter.getParameterizedType());
                parameterInstances[0] = parameterInstance;
                return parameterInstances;
            } catch (IOException e) {
                LOG.error("Read parameter for [JsonType] failed, method name: {}", method.getMethodNameReadable());
                throw new SwaggerDubboException(e.getMessage(), e);
            }
        } else {
            Object[] parameters = new Object[method.getMethod().getParameterCount()];
            Map<String, String> parameterMap;

            if (Strings.isNullOrEmpty(requestBody)) {
                parameterMap = fromRequestMap(requestMap);
            } else {
                parameterMap = fromRequestBody(requestBody);
            }
            int index = 0;
            for (Parameter parameter : method.getMethod().getParameters()) {
                String name = parameter.getName();
                String value = parameterMap.get(name);

                LOG.debug("name: {}, value: {}", name, value);

                Object parameterInstance = null;
                try {
                    if (value != null) {
                        if (Strings.class.equals(parameter.getType())) {
                            parameterInstance = value;
                        } else {
                            parameterInstance = JsonUtils.readValueFromRealType(value, parameter.getParameterizedType());
                        }
                    }
                } catch (IOException e) {
                    LOG.error("Read parameter for [NonJsonType] failed, method name: {}, parameter type: {}, value: {}",
                            method.getMethodNameReadable(),
                            parameter.getType(),
                            value);
                    throw new SwaggerDubboException(e.getMessage(), e);
                }
                parameters[index++] = parameterInstance;
            }

            return parameters;
        }
    }

    private static Map<String, String> fromRequestMap(Map<String, String[]> requestMap) {
        Map<String, String> parameterMap = new HashMap<>();
        for (Map.Entry<String, String[]> entry : requestMap.entrySet()) {
            String value = "";
            if (entry.getValue().length > 0) {
                value = entry.getValue()[0];
            }
            parameterMap.put(entry.getKey(), value);
        }

        return parameterMap;
    }

    private static Map<String, String> fromRequestBody(String requestBody) {
        String[] queries = requestBody.split("&");
        Map<String, String> request = new HashMap<>(queries.length);
        for (String query : queries) {
            String[] key = query.split("=");
            String name = "";
            String value = "";

            if (key.length > 0) {
                name = key[0];
            }

            if (key.length > 1) {
                try {
                    value = URLDecoder.decode(key[1], "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    LOG.error("Decode value: [{}] for name [{}] failed: {}", key[1], name, e.getMessage());
                    throw new SwaggerDubboException(e.getMessage(), e);
                }
            }

            request.putIfAbsent(name, value);
        }

        return request;
    }
}
