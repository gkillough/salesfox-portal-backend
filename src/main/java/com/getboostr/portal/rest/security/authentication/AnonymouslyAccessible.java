package com.getboostr.portal.rest.security.authentication;

import com.getboostr.portal.rest.security.common.SecurityInterface;

public interface AnonymouslyAccessible extends SecurityInterface {
    default String[] anonymouslyAccessibleStaticResourceAntMatchers() {
        return new String[0];
    }

    default String[] anonymouslyAccessibleApiAntMatchers() {
        return new String[0];
    }

}
