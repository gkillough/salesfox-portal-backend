package com.usepipeline.portal.web.security.authentication;

import com.usepipeline.portal.web.security.authentication.user.PortalLoginAttemptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationFailureListener implements ApplicationListener<AuthenticationFailureBadCredentialsEvent> {
    private PortalLoginAttemptService portalLoginAttemptService;

    @Autowired
    public AuthenticationFailureListener(PortalLoginAttemptService portalLoginAttemptService) {
        this.portalLoginAttemptService = portalLoginAttemptService;
    }

    @Override
    public void onApplicationEvent(AuthenticationFailureBadCredentialsEvent event) {
        UserDetails userDetails = (UserDetails) event.getAuthentication().getPrincipal();
        portalLoginAttemptService.addAttempt(userDetails);
    }

}
