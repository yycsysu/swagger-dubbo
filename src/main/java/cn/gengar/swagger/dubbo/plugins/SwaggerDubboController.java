package cn.gengar.swagger.dubbo.plugins;

import cn.gengar.swagger.dubbo.exception.SwaggerDubboException;
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
import java.util.stream.Collectors;

/**
 * @author gengar yu
 */

@RestController
public class SwaggerDubboController {
    private static final Logger LOG = LoggerFactory.getLogger(SwaggerDubboController.class);

    private final DocumentationPluginsBootstrapper bootstrapper;

    @Autowired
    public SwaggerDubboController(DocumentationPluginsBootstrapper bootstrapper) {
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
        try {
            String jsonBody = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
            return DubboMethodInvoker.invoke(key, jsonBody, request.getParameterMap());
        } catch (IOException e) {
            throw new SwaggerDubboException(e.getMessage(), e);
        }
    }
}