package com.usepipeline.portal.web.registration;

import com.usepipeline.portal.web.registration.organization.OrganizationAccountRegistrationModel;
import com.usepipeline.portal.web.registration.user.UserRegistrationModel;
import com.usepipeline.portal.web.registration.user.UserRegistrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RegistrationController {
    private UserRegistrationService userRegistrationService;

    @Autowired
    public RegistrationController(UserRegistrationService userRegistrationService) {
        this.userRegistrationService = userRegistrationService;
    }

    @GetMapping("/registerUser")
    public boolean registerUser(@RequestParam UserRegistrationModel registrationRequest) {
        return userRegistrationService.registerUser(registrationRequest);
    }

    @GetMapping("/registerOrganization")
    public boolean registerOrganizationAccount(@RequestParam OrganizationAccountRegistrationModel registrationRequest) {
        // TODO implement
        return false;
    }

}
