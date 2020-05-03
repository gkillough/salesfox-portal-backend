package com.usepipeline.portal.documentation;

import com.usepipeline.portal.web.security.authorization.AdminOnlyAccessible;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collections;

@Configuration
@EnableSwagger2
public class SwaggerConfiguration implements AdminOnlyAccessible {
    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.usepipeline.portal.web"))
                .build()
                .produces(Collections.singleton("application/json"))
                .consumes(Collections.singleton("application/json"))
                .ignoredParameterTypes(HttpServletRequest.class, HttpServletResponse.class)
                .apiInfo(apiEndPointsInfo());
    }

    private ApiInfo apiEndPointsInfo() {
        return new ApiInfoBuilder()
                .title("Use Pipeline Portal - REST API")
                .build();
    }

    @Override
    public String[] adminOnlyEndpointAntMatchers() {
        return new String[]{
                "/swagger-ui.html",
                "/webjars",
                createSubDirectoryPattern("/webjars"),
                "/swagger-resources",
                createSubDirectoryPattern("/swagger-resources"),
                "/v2/api-docs"
        };
    }

}
