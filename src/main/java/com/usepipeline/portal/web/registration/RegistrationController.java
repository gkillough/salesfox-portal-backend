package com.usepipeline.portal.web.registration;

import com.usepipeline.portal.web.common.model.request.EmailToValidateModel;
import com.usepipeline.portal.web.common.model.response.ValidationResponseModel;
import com.usepipeline.portal.web.organization.invitation.OrganizationInvitationService;
import com.usepipeline.portal.web.registration.organization.OrganizationAccountRegistrationService;
import com.usepipeline.portal.web.registration.organization.model.OrganizationAccountNameToValidateModel;
import com.usepipeline.portal.web.registration.organization.model.OrganizationAccountRegistrationModel;
import com.usepipeline.portal.web.registration.organization.model.OrganizationAccountUserRegistrationModel;
import com.usepipeline.portal.web.registration.user.UserRegistrationModel;
import com.usepipeline.portal.web.registration.user.UserRegistrationService;
import com.usepipeline.portal.web.security.authentication.AnonymouslyAccessible;
import com.usepipeline.portal.web.security.authorization.CsrfIgnorable;
import com.usepipeline.portal.web.security.authorization.PortalAuthorityConstants;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping(RegistrationController.BASE_ENDPOINT)
public class RegistrationController implements CsrfIgnorable, AnonymouslyAccessible {
    public static final String BASE_ENDPOINT = "/register";
    public static final String USER_ENDPOINT_SUFFIX = "/user";
    public static final String ORGANIZATION_ENDPOINT_SUFFIX = "/organization";
    public static final String ORGANIZATION_ACCOUNT_USER_ENDPOINT_SUFFIX = ORGANIZATION_ENDPOINT_SUFFIX + USER_ENDPOINT_SUFFIX;

    private UserRegistrationService userRegistrationService;
    private OrganizationAccountRegistrationService organizationAccountRegistrationService;
    private OrganizationInvitationService organizationInvitationService;

    @Autowired
    public RegistrationController(UserRegistrationService userRegistrationService,
                                  OrganizationAccountRegistrationService organizationAccountRegistrationService, OrganizationInvitationService organizationInvitationService) {
        this.userRegistrationService = userRegistrationService;
        this.organizationAccountRegistrationService = organizationAccountRegistrationService;
        this.organizationInvitationService = organizationInvitationService;
    }

    @ApiOperation(value = "Register a non organization user", response = Boolean.class)
    @PostMapping(USER_ENDPOINT_SUFFIX)
    public boolean registerUser(@ApiParam @RequestBody UserRegistrationModel registrationRequest) {
        userRegistrationService.registerUser(registrationRequest);
        return true;
    }

    @ApiOperation(value = "Register an organization with an initial account owner user", response = Boolean.class)
    @PostMapping(ORGANIZATION_ENDPOINT_SUFFIX)
    public void registerOrganizationAccount(@RequestBody OrganizationAccountRegistrationModel registrationRequest) {
        organizationAccountRegistrationService.registerOrganizationAccount(registrationRequest);
    }

    @ApiOperation(value = "Complete the registration of an organization account user")
    @PostMapping(ORGANIZATION_ACCOUNT_USER_ENDPOINT_SUFFIX)
    @PreAuthorize(PortalAuthorityConstants.CREATE_ORGANIZATION_ACCOUNT_PERMISSION_AUTH_CHECK)
    public void registerOrganizationAccountUser(HttpServletResponse httpServletResponse, @RequestBody OrganizationAccountUserRegistrationModel registrationRequest) {
        organizationInvitationService.completeOrganizationAccountRegistrationFromInvite(httpServletResponse, registrationRequest);
    }

    @ApiOperation(value = "Validate that an email address is eligible to own an organization account")
    @PostMapping("/organization/validate/account_owner")
    public ValidationResponseModel validateOrganizationAccountManager(@RequestBody EmailToValidateModel validationRequest) {
        return organizationAccountRegistrationService.isAccountOwnerEmailValid(validationRequest);
    }

    @ApiOperation(value = "Validate that an account name is eligible to be assigned to an organization")
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
