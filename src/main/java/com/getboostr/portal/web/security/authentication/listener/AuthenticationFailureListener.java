package com.getboostr.portal.web.security.authentication.listener;

import com.getboostr.portal.web.security.authentication.user.PortalUserLoginAttemptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationFailureListener implements ApplicationListener<AuthenticationFailureBadCredentialsEvent> {
    private PortalUserLoginAttemptService portalUserLoginAttemptService;

    @Autowired
    public AuthenticationFailureListener(PortalUserLoginAttemptService portalUserLoginAttemptService) {
        this.portalUserLoginAttemptService = portalUserLoginAttemptService;
    }

    @Override
    public void onApplicationEvent(AuthenticationFailureBadCredentialsEvent event) {
        String username = (String) event.getAuthentication().getPrincipal();
        portalUserLoginAttemptService.recordFailedAttempt(username);
    }

}
