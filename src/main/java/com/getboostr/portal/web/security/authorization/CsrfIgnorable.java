package com.getboostr.portal.web.security.authorization;

import com.getboostr.portal.web.security.common.SecurityInterface;

public interface CsrfIgnorable extends SecurityInterface {
    String[] ignoredEndpointAntMatchers();

}
