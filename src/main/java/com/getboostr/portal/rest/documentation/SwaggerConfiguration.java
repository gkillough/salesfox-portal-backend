package com.getboostr.portal.rest.documentation;

import com.getboostr.portal.rest.security.authentication.AnonymouslyAccessible;
import com.getboostr.portal.rest.security.common.SecurityInterface;
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
import java.util.Set;

@Configuration
@EnableSwagger2
public class SwaggerConfiguration implements AnonymouslyAccessible {
    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.getboostr.portal.rest.api"))
                .build()
                .produces(Set.of("application/json"))
                .consumes(Set.of("application/json"))
                .ignoredParameterTypes(HttpServletRequest.class, HttpServletResponse.class)
                .apiInfo(apiEndPointsInfo());
    }

    private ApiInfo apiEndPointsInfo() {
        return new ApiInfoBuilder()
                .title("Boostr Portal - REST API")
                .build();
    }

    // FIXME fix this before release and make this class implement AdminOnlyAccessible
//    @Override
    public static String[] adminOnlyStaticResourceEndpoints() {
        return new String[] {
                "/swagger-ui.html",
                "/webjars",
                SecurityInterface.createSubDirectoryPattern("/webjars"),
                "/swagger-resources",
                SecurityInterface.createSubDirectoryPattern("/swagger-resources"),
                "/v2/api-docs"
        };
    }

    @Override
    public String[] anonymouslyAccessibleStaticResourceAntMatchers() {
        return adminOnlyStaticResourceEndpoints();
    }

}
