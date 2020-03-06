package com.usepipeline.portal.web.security.authorization;

import com.usepipeline.portal.web.security.common.SecurityInterface;

public interface CsrfIgnorable extends SecurityInterface {
    String[] ignoredEndpointAntMatchers();

}
