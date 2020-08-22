package ai.salesfox.portal.rest.security.authorization;

import ai.salesfox.portal.rest.security.common.SecurityInterface;

public interface CsrfIgnorable extends SecurityInterface {
    String[] csrfIgnorableApiAntMatchers();

}
