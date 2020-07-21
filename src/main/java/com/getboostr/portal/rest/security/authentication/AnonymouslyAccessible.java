package com.getboostr.portal.rest.security.authentication;

import com.getboostr.portal.rest.security.common.SecurityInterface;

public interface AnonymouslyAccessible extends SecurityInterface {
    String[] allowedEndpointAntMatchers();

}
