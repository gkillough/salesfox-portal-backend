package com.usepipeline.portal.web.security.authentication;

import com.usepipeline.portal.web.security.common.SecurityInterface;

public interface AnonymouslyAccessible extends SecurityInterface {
    String[] allowedEndpointAntMatchers();

}
