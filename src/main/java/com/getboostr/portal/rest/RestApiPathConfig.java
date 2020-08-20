package com.getboostr.portal.rest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.HandlerTypePredicate;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class RestApiPathConfig implements WebMvcConfigurer {
    public static final String DEFAULT_API_PATH_PREFIX = "/api";

    public static String addApiPrefix(String endpointAntMatcher) {
        if (!StringUtils.startsWith(endpointAntMatcher, DEFAULT_API_PATH_PREFIX)) {
            return RestApiPathConfig.DEFAULT_API_PATH_PREFIX + endpointAntMatcher;
        }
        return endpointAntMatcher;
    }

    @Override
    public void configurePathMatch(PathMatchConfigurer pathMatchConfigurer) {
        pathMatchConfigurer.addPathPrefix(DEFAULT_API_PATH_PREFIX, HandlerTypePredicate.forAnnotation(RestController.class));
    }

}
