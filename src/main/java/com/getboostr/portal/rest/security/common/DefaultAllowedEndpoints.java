package com.getboostr.portal.rest.security.common;

import com.getboostr.portal.rest.security.authentication.AnonymouslyAccessible;

public class DefaultAllowedEndpoints implements AnonymouslyAccessible {
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
