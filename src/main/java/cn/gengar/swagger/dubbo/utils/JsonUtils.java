package cn.gengar.swagger.dubbo.utils;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * @author gengar yu
 */

public class JsonUtils {
    private static final ObjectMapper objectMapper;

    static {
        objectMapper = new ObjectMapper();
    }

    public static Object readValueFromRealType(final String value,
                                               final Type surfaceType) throws IOException {
        if (surfaceType instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) surfaceType;

            Class<?> parameterizeClass = (Class<?>) parameterizedType.getRawType();
            Class<?>[] actualTypeArgumentsClass = convertToClassArray(parameterizedType.getActualTypeArguments());
            JavaType realType = objectMapper.getTypeFactory().constructParametricType(parameterizeClass, actualTypeArgumentsClass);

            return objectMapper.readValue(value, realType);
        } else {
            return objectMapper.readValue(value, (Class<?>) surfaceType);
        }
    }

    private static Class<?>[] convertToClassArray(Type[] types) {
        Class<?>[] classes = new Class[types.length];

        for (int i = 0; i < types.length; i++) {
            classes[i] = (Class<?>) types[i];
        }

        return classes;
    }
}
