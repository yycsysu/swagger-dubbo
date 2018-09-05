package cn.gengar.swagger.dubbo.plugins;

import com.alibaba.dubbo.config.spring.ReferenceBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import springfox.documentation.RequestHandler;
import springfox.documentation.builders.BuilderDefaults;
import springfox.documentation.spi.service.RequestHandlerProvider;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author gengar yu
 */

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class DubboRequestHandlerProvider implements RequestHandlerProvider {

    private final DubboMethodScanner scanner;
    private final DubboMethodResolver resolver;
    private final DubboReferenceBeanBuilder builder;

    @Value("${swagger.dubbo.reference.lazy:false}")
    private boolean lazy;

    @Autowired
    public DubboRequestHandlerProvider(DubboMethodScanner scanner,
                                       DubboMethodResolver resolver,
                                       DubboReferenceBeanBuilder builder) {
        this.scanner = scanner;
        this.resolver = resolver;
        this.builder = builder;
    }

    @Override
    public List<RequestHandler> requestHandlers() {
        List<RequestHandler> requestHandlers = buildRequestHandlers();
        return BuilderDefaults.nullToEmptyList(requestHandlers);
    }

    private List<RequestHandler> buildRequestHandlers() {
        List<DubboMethod> methods = scanner.scan();

        if (!lazy) {
            reference(methods);
        }

        return toRequestHandlers(methods);
    }

    private void reference(List<DubboMethod> methods) {
        for (DubboMethod method : methods) {
            String key = method.getServiceKey();
            ReferenceBean referenceBean = SwaggerDubboContext.getReferenceBean(key);

            if (referenceBean == null) {
                referenceBean = builder.build(method);
                SwaggerDubboContext.putReferenceBean(key, referenceBean);
            }
        }
    }

    private List<RequestHandler> toRequestHandlers(List<DubboMethod> methods) {
        return methods.stream()
                .map(method -> new DubboRequestHandler(method, resolver))
                .collect(Collectors.toList());
    }

}
