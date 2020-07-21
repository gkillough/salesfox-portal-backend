package com.getboostr.portal.web.security.authorization;

import com.getboostr.portal.web.security.common.SecurityInterface;

public interface AdminOnlyAccessible extends SecurityInterface {
    String[] adminOnlyEndpointAntMatchers();

}
