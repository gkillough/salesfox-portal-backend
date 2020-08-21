package com.getboostr.portal.rest.security.common;

import com.getboostr.portal.PortalConfiguration;
import com.getboostr.portal.rest.security.authentication.AnonymouslyAccessible;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StaticResourceLinks implements AnonymouslyAccessible {
    private final PortalConfiguration portalConfiguration;

    @Autowired
    public StaticResourceLinks(PortalConfiguration portalConfiguration) {
        this.portalConfiguration = portalConfiguration;
    }

    @Override
    public String[] anonymouslyAccessibleStaticResourceAntMatchers() {
        return new String[] {
                portalConfiguration.getResetPasswordLinkSpec(),
                portalConfiguration.getInviteOrganizationAccountUserLinkSpec()
        };
    }

}
