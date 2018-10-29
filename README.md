# Swagger Dubbo

Swagger Dubbo makes Dubbo Service show as Swagger RESTful API, which makes it easy to document and test.

## Dependencies

## Get Started

### Add Maven Dependency

1. Add [Swagger Dependency](https://swagger.io)
2. Add [Dubbo Denpendency](https://dubbo.incubator.apache.org)
3. Add Swagger-Dubbo Denpendency:

```mvn
<dependency>
    <groupId>cn.gengar</groupId>
    <artifactId>swagger-dubbo</artifactId>
    <version>0.0.1-beta</version>
</dependency>
```

### Config

```java
@EnableSwagger2
@EnableSwaggerDubbo
@Configuration
public class SwaggerDubboConfig
{
    @Bean
    public Docket dubboDocket(){
        return new Docket(DocumentationType.SWAGGER_2)
                .groupName("dubbo")
                .select()
                .apis(DubboRequestHandlerSelectors.any()) //get dubbo api only
                .paths(PathSelectors.any())
                .build();
    }
}
```

# License

Copyright [2018] [Gengar Yu]

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at [apache.org/licenses/LICENSE-2.0](http://www.apache.org/licenses/LICENSE-2.0)

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
