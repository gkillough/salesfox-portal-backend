package com.usepipeline.portal.web.registration;

import com.usepipeline.portal.web.common.model.EmailToValidateModel;
import com.usepipeline.portal.web.common.model.ValidationResponseModel;
import com.usepipeline.portal.web.registration.organization.OrganizationAccountRegistrationService;
import com.usepipeline.portal.web.registration.organization.model.OrganizationAccountNameToValidateModel;
import com.usepipeline.portal.web.registration.organization.model.OrganizationAccountRegistrationModel;
import com.usepipeline.portal.web.registration.organization.model.OrganizationAccountUserRegistrationModel;
import com.usepipeline.portal.web.registration.user.UserRegistrationModel;
import com.usepipeline.portal.web.registration.user.UserRegistrationService;
import com.usepipeline.portal.web.security.authentication.AnonymousAccessible;
import com.usepipeline.portal.web.security.authorization.CsrfIgnorable;
import com.usepipeline.portal.web.security.authorization.PortalAuthorityConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(RegistrationController.BASE_ENDPOINT)
public class RegistrationController implements CsrfIgnorable, AnonymousAccessible {
    public static final String BASE_ENDPOINT = "/register";
    public static final String USER_ENDPOINT_SUFFIX = "/user";
    public static final String ORGANIZATION_ENDPOINT_SUFFIX = "/organization";
    public static final String ORGANIZATION_ACCOUNT_USER_ENDPOINT_SUFFIX = ORGANIZATION_ENDPOINT_SUFFIX + USER_ENDPOINT_SUFFIX;

    private UserRegistrationService userRegistrationService;
    private OrganizationAccountRegistrationService organizationAccountRegistrationService;

    @Autowired
    public RegistrationController(UserRegistrationService userRegistrationService,
                                  OrganizationAccountRegistrationService organizationAccountRegistrationService) {
        this.userRegistrationService = userRegistrationService;
        this.organizationAccountRegistrationService = organizationAccountRegistrationService;
    }

    @PostMapping(USER_ENDPOINT_SUFFIX)
    public boolean registerUser(@RequestBody UserRegistrationModel registrationRequest) {
        userRegistrationService.registerUser(registrationRequest);
        return true;
    }

    @PostMapping(ORGANIZATION_ENDPOINT_SUFFIX)
    public void registerOrganizationAccount(@RequestBody OrganizationAccountRegistrationModel registrationRequest) {
        organizationAccountRegistrationService.registerOrganizationAccount(registrationRequest);
    }

    @PostMapping(ORGANIZATION_ACCOUNT_USER_ENDPOINT_SUFFIX)
    @PreAuthorize(PortalAuthorityConstants.CREATE_ORGANIZATION_ACCOUNT_PERMISSION_AUTH_CHECK)
    public void registerOrganizationAccountUser(@RequestBody OrganizationAccountUserRegistrationModel registrationRequest) {
        // FIXME implement
    }

    @PostMapping("/organization/validate/account_owner")
    public ValidationResponseModel validateOrganizationAccountManager(@RequestBody EmailToValidateModel validationRequest) {
        return organizationAccountRegistrationService.isAccountOwnerEmailValid(validationRequest);
    }

    @PostMapping("/organization/validate/account_name")
    public ValidationResponseModel validateOrganizationAccountName(@RequestBody OrganizationAccountNameToValidateModel validationRequest) {
        return organizationAccountRegistrationService.isOrganizationAccountNameValid(validationRequest);
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
