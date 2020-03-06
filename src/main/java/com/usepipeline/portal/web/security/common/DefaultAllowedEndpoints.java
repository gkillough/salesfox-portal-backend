package com.usepipeline.portal.web.security.common;

import com.usepipeline.portal.web.security.authentication.AnonymousAccessible;

public class DefaultAllowedEndpoints implements AnonymousAccessible {
    public static final String LOGIN_ENDPOINT = "/login";
    public static final String LOGOUT_ENDPOINT = "/logout";
    public static final String CSS_DIRECTORIES = "static/css/**";

    @Override
    public String[] allowedEndpointAntMatchers() {
        return new String[]{
                DefaultAllowedEndpoints.LOGIN_ENDPOINT,
                DefaultAllowedEndpoints.LOGOUT_ENDPOINT,
                DefaultAllowedEndpoints.CSS_DIRECTORIES
        };
    }

}
