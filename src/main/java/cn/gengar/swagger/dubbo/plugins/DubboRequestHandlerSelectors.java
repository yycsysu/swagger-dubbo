package cn.gengar.swagger.dubbo.plugins;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import springfox.documentation.RequestHandler;
import springfox.documentation.spring.web.plugins.CombinedRequestHandler;

/**
 * @author gengar yu
 */

public class DubboRequestHandlerSelectors {
    private DubboRequestHandlerSelectors() {
        throw new UnsupportedOperationException();
    }


    /**
     * Any Dubbo RequestHandler satisfies this condition
     *
     * @return predicate that is true if is a Dubbo RequestHandler
     */
    public static Predicate<RequestHandler> any() {
        return input -> isDubboRequestHandler(input);
    }

    /**
     * Any Dubbo RequestHandler satisfies this condition
     *
     * @return predicate that is true if is not a Dubbo RequestHandler
     */
    public static Predicate<RequestHandler> none() {
        return input -> !isDubboRequestHandler(input);
    }

    private static boolean isDubboRequestHandler(final RequestHandler input) {
        return input instanceof DubboRequestHandler;
    }
}

