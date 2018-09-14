package cn.gengar.swagger.dubbo.exception;

/**
 * @author gengar yu
 */

public class SwaggerDubboException extends RuntimeException {
    public SwaggerDubboException() { super(); }
    public SwaggerDubboException(String s) { super(s); }
    public SwaggerDubboException(String s, Exception e) { super(s, e); }
}
