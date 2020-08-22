package ai.salesfox.portal.rest.security.authentication;

import ai.salesfox.portal.rest.security.common.SecurityInterface;

public interface AnonymouslyAccessible extends SecurityInterface {
    default String[] anonymouslyAccessibleStaticResourceAntMatchers() {
        return new String[0];
    }

    default String[] anonymouslyAccessibleApiAntMatchers() {
        return new String[0];
    }

}
