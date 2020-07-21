package com.getboostr.portal.rest.security.authorization;

import com.getboostr.portal.rest.security.common.SecurityInterface;

public interface AdminOnlyAccessible extends SecurityInterface {
    String[] adminOnlyEndpointAntMatchers();

}
