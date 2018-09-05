package cn.gengar.swagger.dubbo.utils;

/**
 * @author gengar yu
 */

public class ClassUtils {

    public static boolean isPrimitiveOrWrapperOrString(Class<?> clazz) {
        return org.springframework.util.ClassUtils.isPrimitiveOrWrapper(clazz)
                || String.class.isAssignableFrom(clazz);
    }

    public static Class<?> getUserClass(Object ref) {
        return org.springframework.util.ClassUtils.getUserClass(ref);
    }
}
