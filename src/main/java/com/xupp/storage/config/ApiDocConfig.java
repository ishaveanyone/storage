package com.xupp.storage.config;

import com.google.common.base.Predicate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * @author yangmin
 * @date 2018/8/15
 * @desc
 */
@Configuration
@EnableSwagger2
public class ApiDocConfig {

    @Bean
    public Docket omsApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .groupName("storageApi")
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.xupp.storage"))
                .paths(PathSelectors.any())
                .build();
    }
//    @Bean
//    public Docket processApi() {
//        return new Docket(DocumentationType.SWAGGER_2)
//                .groupName("processApi")
//                .select()
//                .apis(RequestHandlerSelectors.basePackage("com.dist.dbpm.task.rest"))
//                .paths(PathSelectors.any())
//                .build();
//    }
//
//    @Bean
//    public Docket formApi(){
//        return new Docket(DocumentationType.SWAGGER_2)
//                .groupName("formApi")
//                .select()
//                .apis(RequestHandlerSelectors.basePackage("com.dist.dbpm.form.rest"))
//                .paths(PathSelectors.any())
//                .build();
//    }

    private Predicate<String> paths(){
        return PathSelectors.regex("^/(?!error).*$");
    }

    private ApiInfo apiInfo(){
        Contact contact = new Contact("DIST", "https://github.com/DistX", "yangmin@com.dist.com.cn");
        return new ApiInfoBuilder()
                .title("文档模块")
                .description("后台API文档")
                .contact(contact)
                .version("1.0")
                .build();
    }
}
