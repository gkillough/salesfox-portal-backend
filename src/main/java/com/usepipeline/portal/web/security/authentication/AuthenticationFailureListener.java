package com.usepipeline.portal.web.security.authentication;

import com.usepipeline.portal.web.security.authentication.user.PortalUserLoginAttemptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.core.userdetails.UserDetails;
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
        UserDetails userDetails = (UserDetails) event.getAuthentication().getPrincipal();
        boolean maximumAttemptsExceeded = portalUserLoginAttemptService.addAttempt(userDetails);
        if (maximumAttemptsExceeded) {
            // TODO schedule a task to decrement attempts after a certain number of minutes
        }
    }

}
