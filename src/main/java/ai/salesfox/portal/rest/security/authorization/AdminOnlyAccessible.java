package ai.salesfox.portal.rest.security.authorization;

import ai.salesfox.portal.rest.security.common.SecurityInterface;

public interface AdminOnlyAccessible extends SecurityInterface {
    default String[] adminOnlyStaticResourceAntMatchers() {
        return new String[0];
    }

    default String[] adminOnlyApiAntMatchers() {
        return new String[0];
    }

}
