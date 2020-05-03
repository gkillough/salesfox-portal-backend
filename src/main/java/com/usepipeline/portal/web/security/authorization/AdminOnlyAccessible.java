package com.usepipeline.portal.web.security.authorization;

import com.usepipeline.portal.web.security.common.SecurityInterface;

public interface AdminOnlyAccessible extends SecurityInterface {
    String[] adminOnlyEndpointAntMatchers();

}
