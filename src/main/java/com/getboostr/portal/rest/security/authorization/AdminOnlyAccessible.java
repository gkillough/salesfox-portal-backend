package com.getboostr.portal.rest.security.authorization;

import com.getboostr.portal.rest.security.common.SecurityInterface;

public interface AdminOnlyAccessible extends SecurityInterface {
    default String[] adminOnlyStaticResourceAntMatchers() {
        return new String[0];
    }

    default String[] adminOnlyApiAntMatchers() {
        return new String[0];
    }

}
