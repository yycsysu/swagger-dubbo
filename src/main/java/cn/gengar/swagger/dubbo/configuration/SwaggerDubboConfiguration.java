package cn.gengar.swagger.dubbo.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * @author gengar yu
 */

@Configuration
@ComponentScan(basePackages = {
        "cn.gengar.swagger.dubbo.plugins"
})
public class SwaggerDubboConfiguration {

}
