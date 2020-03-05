package com.usepipeline.portal.web.security.authentication;

import com.usepipeline.portal.web.security.common.SecurityInterface;

public interface AnonymousAccessible extends SecurityInterface {
    String[] allowedEndpointAntMatchers();

}
