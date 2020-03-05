package com.usepipeline.portal.web.registration;

import com.usepipeline.portal.web.registration.organization.OrganizationAccountRegistrationModel;
import com.usepipeline.portal.web.registration.user.UserRegistrationModel;
import com.usepipeline.portal.web.registration.user.UserRegistrationService;
import com.usepipeline.portal.web.security.authentication.AnonymousAccessible;
import com.usepipeline.portal.web.security.authorization.CsrfIgnorable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(RegistrationController.BASE_ENDPOINT)
public class RegistrationController implements CsrfIgnorable, AnonymousAccessible {
    public static final String BASE_ENDPOINT = "/register";
    private UserRegistrationService userRegistrationService;

    @Autowired
    public RegistrationController(UserRegistrationService userRegistrationService) {
        this.userRegistrationService = userRegistrationService;
    }

    @PostMapping("/user")
    public boolean registerUser(@RequestBody UserRegistrationModel registrationRequest) {
        return userRegistrationService.registerUser(registrationRequest);
    }

    @PostMapping("/organization")
    public boolean registerOrganizationAccount(@RequestBody OrganizationAccountRegistrationModel registrationRequest) {
        // TODO implement
        return false;
    }

    @Override
    public String[] ignoredEndpointAntMatchers() {
        return new String[]{
                createSubDirectoryPattern(RegistrationController.BASE_ENDPOINT)
        };
    }

    @Override
    public String[] allowedEndpointAntMatchers() {
        return new String[]{
                createSubDirectoryPattern(RegistrationController.BASE_ENDPOINT)
        };
    }

}
