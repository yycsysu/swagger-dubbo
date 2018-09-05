package cn.gengar.swagger.dubbo.plugins;

import com.alibaba.dubbo.config.spring.ReferenceBean;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.spring.web.plugins.DocumentationPluginsBootstrapper;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author gengar yu
 */

@RestController
public class SwaggerDubboController {
    private static final Logger LOG = LoggerFactory.getLogger(SwaggerDubboController.class);

    private final ObjectMapper objectMapper;

    private final DocumentationPluginsBootstrapper bootstrapper;

    @Autowired
    public SwaggerDubboController(ObjectMapper objectMapper,
                                  DocumentationPluginsBootstrapper bootstrapper) {
        this.objectMapper = objectMapper;
        this.bootstrapper = bootstrapper;
    }

    @PostMapping(value = "/swagger-dubbo-api/{service}/{method}")
    public Object call(@PathVariable String service,
                       @PathVariable String method,
                       HttpServletRequest request, HttpServletResponse response) {
        String key = service + "-" + method;
        return handleRequestThenResponse(key, request, response);
    }

    @PostMapping(value = "/swagger-dubbo-api/{service}/{method}/{order}")
    public Object call(@PathVariable String service,
                       @PathVariable String method,
                       @PathVariable String order,
                       HttpServletRequest request, HttpServletResponse response) {
        String key = service + "-" + method + "-" + order;
        return handleRequestThenResponse(key, request, response);
    }

    @GetMapping(value = "/swagger-dubbo-api/reboot")
    synchronized public String reboot () {
        if (bootstrapper.isRunning()) {
            bootstrapper.stop();
        }
        bootstrapper.start();

        return "SUCCESS";
    }

    private Object handleRequestThenResponse(String key, HttpServletRequest request, HttpServletResponse response) {
        String jsonBody = "";
        try {
            jsonBody = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
            LOG.debug("request: " + jsonBody);
            LOG.debug("parameter: " + objectMapper.writeValueAsString(request.getParameterMap()));
        } catch (IOException e) {
            e.printStackTrace();
        }

        DubboMethod method = SwaggerDubboContext.getMethod(key);
        ReferenceBean referenceBean = SwaggerDubboContext.getReferenceBean(method.getServiceKey());

        Object[] parameters = transformParameters(method, jsonBody, request.getParameterMap());
        Object target = referenceBean.get();

        try {
            Method targetMethod = target.getClass().getMethod(method.getMethod().getName(), method.getMethod().getParameterTypes());
            return targetMethod.invoke(target, parameters);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            LOG.error("Invoke target method: {} failed: {}", method.getMethod().getName(), e.getMessage());

            return null;
        }
    }

    private Object[] transformParameters(DubboMethod method, String requestBody, Map<String, String[]> requestMap) {
        if (method.isJsonType()) {
            Object[] parameters = new Object[1];
            Class<?>[] types = method.getMethod().getParameterTypes();
            Class<?> type = types.length > 0 ? types[0] : Object.class;

            try {
                Object parameter = objectMapper.readValue(requestBody, type);
                parameters[0] = parameter;
                return parameters;
            } catch (IOException e) {
                LOG.warn("read parameter for method: {} from request body failed: {}", method, e.getMessage());
                return parameters;
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
                        parameterInstance = objectMapper.readValue(value, parameter.getType());
                    }
                } catch (IOException e) {
                    LOG.warn("parse value: {} to type: {} failed: {}.", value, parameter.getType(), e.getMessage());
                    parameterInstance = value;
                }
                parameters[index++] = parameterInstance;
            }

            return parameters;
        }
    }

    private Map<String,String> fromRequestMap(Map<String,String[]> requestMap) {
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

    private Map<String, String> fromRequestBody(String requestBody) {
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
                    LOG.warn("decode: [{}] failed: {}", key[1], e.getMessage());
                    value = key[1];
                }
            }

            request.putIfAbsent(name, value);
        }

        return request;
    }

}
