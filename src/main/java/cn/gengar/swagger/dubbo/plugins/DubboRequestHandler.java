package cn.gengar.swagger.dubbo.plugins;

import cn.gengar.swagger.dubbo.utils.ClassUtils;
import com.fasterxml.classmate.ResolvedType;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import io.swagger.annotations.ApiOperation;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ValueConstants;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.condition.NameValueExpression;
import org.springframework.web.servlet.mvc.condition.PatternsRequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import springfox.documentation.RequestHandler;
import springfox.documentation.RequestHandlerKey;
import springfox.documentation.service.ResolvedMethodParameter;

import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.*;

/**
 * @author gengar yu
 */

public class DubboRequestHandler implements RequestHandler {

    private final DubboMethod method;
    private final DubboMethodResolver methodResolver;

    public DubboRequestHandler(DubboMethod method, DubboMethodResolver methodResolver) {
        this.method = method;
        this.methodResolver = methodResolver;
    }

    @Override
    public Class<?> declaringClass() {
        return method.getServiceInstanceClass();
    }

    @Override
    public <T extends Annotation> Optional<T> findAnnotation(Class<T> annotation) {
        return Optional.fromNullable(AnnotationUtils.findAnnotation(method.getMethod(), annotation));
    }

    @Override
    public boolean isAnnotatedWith(Class<? extends Annotation> annotation) {
        return null != AnnotationUtils.findAnnotation(method.getMethod(), annotation);
    }

    @Override
    public <T extends Annotation> Optional<T> findControllerAnnotation(Class<T> annotation) {
        return Optional.fromNullable(AnnotationUtils.findAnnotation(method.getServiceInstanceClass(), annotation));
    }

    @Override
    public PatternsRequestCondition getPatternsCondition() {
        Optional<ApiOperation> optional = findAnnotation(ApiOperation.class);
        String nickName;
        if (optional.isPresent()
                && !"".equals((nickName = optional.get().nickname()))) {

            SwaggerDubboContext.bindMethod(method.getMethodKey(), method.getMethodKey(nickName));

            return new PatternsRequestCondition(method.getServicePath(nickName));
        } else {
            return new PatternsRequestCondition(method.getServicePath());
        }
    }

    @Override
    public String groupName() {
        // interface name as group
        return method.getServiceKey();
    }

    @Override
    public String getName() {
        // method api name
        return method.getMethodNameReadable();
    }

    @Override
    public Set<RequestMethod> supportedMethods() {
        return Sets.newHashSet(RequestMethod.POST);
    }

    @Override
    public Set<? extends MediaType> produces() {
        return Sets.newHashSet(MediaType.APPLICATION_JSON_UTF8);
    }

    @Override
    public Set<? extends MediaType> consumes() {
        if (method.isJsonType()) {
            return Sets.newHashSet(MediaType.APPLICATION_JSON_UTF8);
        } else {
            return Sets.newHashSet(MediaType.APPLICATION_FORM_URLENCODED);
        }
    }

    @Override
    public Set<NameValueExpression<String>> headers() {
        return Sets.newHashSet();
    }

    @Override
    public Set<NameValueExpression<String>> params() {
        return Sets.newHashSet();
    }


    @Override
    public RequestHandlerKey key() {
        return new RequestHandlerKey(
                getPatternsCondition().getPatterns(),
                supportedMethods(),
                consumes(),
                produces());
    }

    @Override
    public List<ResolvedMethodParameter> getParameters() {
        List<ResolvedMethodParameter> list = Lists.newArrayList();
        Parameter[] parameters = method.getMethod().getParameters();
        int index = 0;
        for (Parameter parameter : parameters) {
            List<Annotation> annotations = Lists.newArrayList(parameter.getAnnotations());
            Type type =parameter.getParameterizedType();

            if (method.isJsonType()) {
                annotations.add(requestBody());
            } else if (parameter.getType().isPrimitive()) {
                annotations.add(requestParam());
            } else if (!ClassUtils.isPrimitiveOrWrapperOrString(parameter.getType())
                    && !parameter.getType().isAssignableFrom(Collection.class)) {
                type = Object.class;
            }


            ResolvedMethodParameter item = new ResolvedMethodParameter(
                    index++,
                    parameter.getName(),
                    annotations,
                    methodResolver.resolve(type)
            );
            list.add(item);
        }

        return list;
    }

    @Override
    public ResolvedType getReturnType() {
        return methodResolver.resolve(method.getMethod().getReturnType());
    }


    @Override
    public RequestMappingInfo getRequestMapping() {
        return null;
    }

    @Override
    public HandlerMethod getHandlerMethod() {
        throw new UnsupportedOperationException("Dubbo Request Handler unsupported this method.");
    }

    @Override
    public RequestHandler combine(RequestHandler other) {
        throw new UnsupportedOperationException("Dubbo Request Handler unsupported this method.");
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("DubboRequestHandler{");
        sb.append("key=").append(key());
        sb.append('}');
        return sb.toString();
    }

    private Annotation requestBody() {
        return new RequestBody() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return RequestBody.class;
            }

            @Override
            public boolean required() {
                return true;
            }
        };
    }

    private Annotation requestParam() {
        return new RequestParam() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return RequestParam.class;
            }

            @Override
            public String value() {
                return "";
            }

            @Override
            public String name() {
                return "";
            }

            @Override
            public boolean required() {
                return true;
            }

            @Override
            public String defaultValue() {
                return ValueConstants.DEFAULT_NONE;
            }
        };
    }
}

