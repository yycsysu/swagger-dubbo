package cn.gengar.swagger.dubbo.plugins;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.config.ApplicationConfig;
import com.alibaba.dubbo.config.spring.ReferenceBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * @author gengar yu
 */

@Component
public class DubboReferenceBeanBuilder {

    private final ApplicationContext applicationContext;

    @Autowired
    public DubboReferenceBeanBuilder(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public <T> ReferenceBean<T> build(DubboMethod method) {
        Map<String, String> params = new HashMap<>();
        params.put("group", method.getGroup());
        params.put("version", method.getVersion());

        URL url = new URL(method.getProtocol(), method.getHost(), method.getPort(), method.getServiceInterface(), params);

        return build(url);
    }

    public <T> ReferenceBean<T> build(URL url)
    {
        ReferenceBean<T> referenceBean = new ReferenceBean<T>();

        referenceBean.setUrl(url.toFullString());
        referenceBean.setCheck(false);
        referenceBean.setInterface(url.getServiceInterface());


        referenceBean.setInterface(url.getServiceInterface());
        ApplicationConfig applicationConfig = new ApplicationConfig();
        applicationConfig.setName("Swagger-Dubbo-App");
        referenceBean.setApplication(applicationConfig);

        referenceBean.setApplicationContext(this.applicationContext);

        return referenceBean;
    }

}
