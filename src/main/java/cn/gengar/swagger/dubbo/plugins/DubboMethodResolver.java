package cn.gengar.swagger.dubbo.plugins;

import com.fasterxml.classmate.ResolvedType;
import com.fasterxml.classmate.TypeResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;

/**
 * @author gengar yu
 */

@Component
public class DubboMethodResolver {

    private final TypeResolver typeResolver;

    @Autowired
    public DubboMethodResolver(TypeResolver typeResolver) {
        this.typeResolver = typeResolver;
    }

    public ResolvedType resolve(Type type, Type... typeParameters) {
        return typeResolver.resolve(type, typeParameters);
    }
}
