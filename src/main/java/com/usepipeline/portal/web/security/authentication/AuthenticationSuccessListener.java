package com.usepipeline.portal.web.security.authentication;

import com.usepipeline.portal.web.security.authentication.user.PortalLoginAttemptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationSuccessListener implements ApplicationListener<AuthenticationSuccessEvent> {
    private PortalLoginAttemptService portalLoginAttemptService;

    @Autowired
    public AuthenticationSuccessListener(PortalLoginAttemptService portalLoginAttemptService) {
        this.portalLoginAttemptService = portalLoginAttemptService;
    }

    @Override
    public void onApplicationEvent(AuthenticationSuccessEvent event) {
        UserDetails userDetails = (UserDetails) event.getAuthentication().getPrincipal();
        portalLoginAttemptService.resetAttempts(userDetails);
    }

}
