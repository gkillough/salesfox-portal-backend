package com.getboostr.portal.web.security.authentication;

import com.getboostr.portal.web.security.common.SecurityInterface;

public interface AnonymouslyAccessible extends SecurityInterface {
    String[] allowedEndpointAntMatchers();

}
