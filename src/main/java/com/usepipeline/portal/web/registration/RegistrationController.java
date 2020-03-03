package com.usepipeline.portal.web.registration;

import com.usepipeline.portal.web.registration.organization.OrganizationAccountRegistrationModel;
import com.usepipeline.portal.web.registration.user.UserRegistrationModel;
import com.usepipeline.portal.web.registration.user.UserRegistrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(RegistrationController.BASE_ENDPOINT)
public class RegistrationController {
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

}
