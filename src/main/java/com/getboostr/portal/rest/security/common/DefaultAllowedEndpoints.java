package com.getboostr.portal.rest.security.common;

import com.getboostr.portal.rest.security.authentication.AnonymouslyAccessible;
import org.springframework.stereotype.Component;

@Component
public class DefaultAllowedEndpoints implements AnonymouslyAccessible {
    public static final String ROOT_ENDPOINT = "/";
    public static final String ERROR_ENDPOINT = "/error";

    public static final String LOGIN_ENDPOINT = "/api/login";
    public static final String LOGOUT_ENDPOINT = "/api/logout";

    public static final String STATIC_RESOURCES_DIR = "/static";
    public static final String STATIC_RESOURCES_CONTENTS = "/static/**";

    public static final String FAV_ICON = "/favicon.ico";

    public static final String HTML_CONTENT = "/*.html";
    public static final String HTML_SUB_CONTENT = "**/*.html";

    public static final String JAVASCRIPT_CONTENT = "/**/*.js";
    public static final String JAVASCRIPT_SUB_CONTENT = "/**/*.js";

    public static final String CSS_CONTENT = "/*.css";
    public static final String CSS_SUB_CONTENT = "/**/*.css";

    public static final String PNG_CONTENT = "/*.png";
    public static final String PNG_SUB_CONTENT = "/**/*.png";

    public static final String JPG_CONTENT = "/*.jpg";
    public static final String JPG_SUB_CONTENT = "/**/*.jpg";

    @Override
    public String[] allowedEndpointAntMatchers() {
        return new String[] {
                ROOT_ENDPOINT,
                ERROR_ENDPOINT,

                LOGIN_ENDPOINT,
                LOGOUT_ENDPOINT,

                STATIC_RESOURCES_DIR,
                STATIC_RESOURCES_CONTENTS,

                FAV_ICON,
                HTML_CONTENT,
                HTML_SUB_CONTENT,

                JAVASCRIPT_CONTENT,
                JAVASCRIPT_SUB_CONTENT,

                CSS_CONTENT,
                CSS_SUB_CONTENT,

                PNG_CONTENT,
                PNG_SUB_CONTENT,

                JPG_CONTENT,
                JPG_SUB_CONTENT
        };
    }

}
