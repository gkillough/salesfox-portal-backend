package ai.salesfox.portal.rest.security.authentication.listener;

import ai.salesfox.portal.rest.security.authentication.user.PortalUserLoginAttemptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationSuccessListener implements ApplicationListener<AuthenticationSuccessEvent> {
    private PortalUserLoginAttemptService portalUserLoginAttemptService;

    @Autowired
    public AuthenticationSuccessListener(PortalUserLoginAttemptService portalUserLoginAttemptService) {
        this.portalUserLoginAttemptService = portalUserLoginAttemptService;
    }

    @Override
    public void onApplicationEvent(AuthenticationSuccessEvent event) {
        UserDetails userDetails = (UserDetails) event.getAuthentication().getPrincipal();
        portalUserLoginAttemptService.recordSuccessfulAttempt(userDetails.getUsername());
    }

}
