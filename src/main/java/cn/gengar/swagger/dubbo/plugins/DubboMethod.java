package cn.gengar.swagger.dubbo.plugins;

import cn.gengar.swagger.dubbo.utils.ClassUtils;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.config.model.ProviderMethodModel;
import com.alibaba.dubbo.config.model.ProviderModel;
import com.google.common.base.Strings;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author gengar yu
 */

public class DubboMethod {
    private final String classNamePatternString = "[a-zA-Z]+[0-9a-zA-Z_]*(\\.[a-zA-Z]+[0-9a-zA-Z_]*)*\\.([a-zA-Z]+[0-9a-zA-Z_]*(\\$[a-zA-Z]+[0-9a-zA-Z_]*)*)";

    private final String API_ROUTE = "swagger-dubbo-api";

    private Class<?> serviceInstanceClass;
    private String serviceInterface;
    private Method method;

    private String protocol;
    private String host;
    private int port;

    private String group;
    private String version;
    private String password;
    private String username;

    private String context;

    private int order;
    private boolean hasOverload;

    public DubboMethod(Object ref,
                       URL url,
                       Method method,
                       int order,
                       boolean hasOverload,
                       String context) {
        if (ref != null) {
            this.serviceInstanceClass = ClassUtils.getUserClass(ref);
        } else {
            this.serviceInstanceClass = method.getDeclaringClass();
        }

        this.serviceInterface = url.getServiceInterface();

        this.method = initMethod(method);

        this.protocol = url.getProtocol();
        this.host = url.getHost();
        this.port = url.getPort();
        this.password = url.getPassword();
        this.username = url.getUsername();

        this.group = url.getParameter("group");
        this.version = url.getParameter("version");

        this.order = order;
        this.context = context;
        this.hasOverload = hasOverload;
    }

    public DubboMethod(ProviderModel model,
                       URL url,
                       ProviderMethodModel method,
                       int order,
                       boolean hasOverload,
                       String context) {
        this.serviceInstanceClass = model.getServiceInstance().getClass();

        this.serviceInterface = url.getServiceInterface();

        this.method = initMethod(method.getMethod());

        this.protocol = url.getProtocol();
        this.host = url.getHost();
        this.port = url.getPort();
        this.password = url.getPassword();
        this.username = url.getUsername();

        this.group = url.getParameter("group");
        this.version = url.getParameter("version");

        this.order = order;
        this.context = context;
        this.hasOverload = hasOverload;
    }

    private Method initMethod(Method method) {
        try {
            return serviceInstanceClass
                    .getMethod(
                            method.getName(),
                            method.getParameterTypes()
                    );
        } catch (NoSuchMethodException e) {
            return method;
        }
    }

    public String getServicePath() {
        String path = baseServicePath();
        if (this.hasOverload) {
            path += "/" + this.order;
        }

        return path;
    }

    public String getServicePath(String alias) {
        String path = baseServicePath();
        return path + "/" + alias;
    }

    private String baseServicePath() {
        String path = "";
        if (!Strings.isNullOrEmpty(context)) {
            path += "/" + context;
        }
        return path
                + "/" + API_ROUTE
                + "/" + this.getServiceKey()
                + "/" + method.getName();
    }


    public String getServiceKey() {
        String key = "";
        if (!Strings.isNullOrEmpty(this.group)) {
            key += this.group + ":";
        }
        key += this.serviceInterface;
        if (!Strings.isNullOrEmpty(this.version)) {
            key += ":" + this.version;
        }

        return key;
    }

    public String getMethodNameReadable() {
        StringBuilder name = new StringBuilder(method.getName());
        name.append("(");
        int first = 0;
        for (Parameter type : method.getParameters()) {
            if (first == 0) {
                first = 1;
            } else {
                name.append(", ");
            }
            name.append(getTypeName(type));
        }
        name.append(")");

        return name.toString();
    }

    private String getTypeName(Parameter type) {
        String name = type.getParameterizedType().getTypeName();
        Pattern classNamePattern = Pattern.compile(classNamePatternString);
        Matcher matcher = classNamePattern.matcher(name);

        StringBuilder sb = new StringBuilder();
        int index = 0;
        while (matcher.find()) {
            sb.append(name, index, matcher.start());
            sb.append(matcher.group(2));
            index = matcher.end();
        }

        sb.append(name, index, name.length());

        return sb.toString();
    }

    public String getMethodKey() {
        String key = baseMethodKey();

        if (this.hasOverload) {
            key += "-" + this.order;
        }

        return key;
    }

    public String getMethodKey(String nickName) {
        return baseMethodKey() + "-" + nickName;
    }

    private String baseMethodKey() {
        return this.getServiceKey()
                + "-" + this.method.getName();
    }

    public boolean isJsonType() {
        boolean isJsonType =
                this.method.getParameterTypes().length == 1
                        && !ClassUtils.isPrimitiveOrWrapperOrString(this.method.getParameterTypes()[0]);

        return isJsonType;
    }

    public Class<?> getServiceInstanceClass() {
        return serviceInstanceClass;
    }

    public Method getMethod() {
        return method;
    }

    public String getProtocol() {
        return protocol;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getGroup() {
        return group;
    }

    public String getVersion() {
        return version;
    }

    public String getContext() {
        return context;
    }

    public int getOrder() {
        return order;
    }

    public boolean isHasOverload() {
        return hasOverload;
    }

    public String getServiceInterface() {
        return serviceInterface;
    }

    public String getPassword() {
        return password;
    }

    public String getUsername() {
        return username;
    }
}
