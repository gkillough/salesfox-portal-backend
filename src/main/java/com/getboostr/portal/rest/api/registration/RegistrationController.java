package com.getboostr.portal.rest.api.registration;

import com.getboostr.portal.rest.api.common.model.request.EmailToValidateModel;
import com.getboostr.portal.rest.api.common.model.response.ValidationResponseModel;
import com.getboostr.portal.rest.api.organization.invitation.OrganizationInvitationService;
import com.getboostr.portal.rest.api.registration.organization.OrganizationAccountRegistrationService;
import com.getboostr.portal.rest.api.registration.organization.model.OrganizationAccountNameToValidateModel;
import com.getboostr.portal.rest.api.registration.organization.model.OrganizationAccountRegistrationModel;
import com.getboostr.portal.rest.api.registration.organization.model.OrganizationAccountUserRegistrationModel;
import com.getboostr.portal.rest.api.registration.plan.MultiPlanModel;
import com.getboostr.portal.rest.api.registration.plan.RegistrationPlanService;
import com.getboostr.portal.rest.api.registration.user.UserRegistrationModel;
import com.getboostr.portal.rest.api.registration.user.UserRegistrationService;
import com.getboostr.portal.rest.security.authentication.AnonymouslyAccessible;
import com.getboostr.portal.rest.security.authorization.CsrfIgnorable;
import com.getboostr.portal.rest.security.authorization.PortalAuthorityConstants;
import com.getboostr.portal.rest.security.common.SecurityInterface;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping(RegistrationController.BASE_ENDPOINT)
public class RegistrationController implements CsrfIgnorable, AnonymouslyAccessible {
    public static final String BASE_ENDPOINT = "/register";
    public static final String USER_ENDPOINT_SUFFIX = "/user";
    public static final String ORGANIZATION_ENDPOINT_SUFFIX = "/organization";
    public static final String ORGANIZATION_ACCOUNT_USER_ENDPOINT_SUFFIX = ORGANIZATION_ENDPOINT_SUFFIX + USER_ENDPOINT_SUFFIX;

    private final UserRegistrationService userRegistrationService;
    private final RegistrationPlanService registrationPlanService;
    private final OrganizationAccountRegistrationService organizationAccountRegistrationService;
    private final OrganizationInvitationService organizationInvitationService;

    @Autowired
    public RegistrationController(UserRegistrationService userRegistrationService, RegistrationPlanService registrationPlanService,
                                  OrganizationAccountRegistrationService organizationAccountRegistrationService, OrganizationInvitationService organizationInvitationService) {
        this.userRegistrationService = userRegistrationService;
        this.registrationPlanService = registrationPlanService;
        this.organizationAccountRegistrationService = organizationAccountRegistrationService;
        this.organizationInvitationService = organizationInvitationService;
    }

    @ApiOperation(value = "Register an individual (non-organization) user", response = Boolean.class)
    @PostMapping(USER_ENDPOINT_SUFFIX)
    public boolean registerUser(@ApiParam @RequestBody UserRegistrationModel registrationRequest) {
        userRegistrationService.registerUser(registrationRequest);
        return true;
    }

    @GetMapping("/plans")
    public MultiPlanModel getUserRegistrationPlans() {
        return registrationPlanService.getPlans();
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
    public String[] anonymouslyAccessibleApiEndpoints() {
        return new String[] {
                SecurityInterface.createSubDirectoryPattern(RegistrationController.BASE_ENDPOINT)
        };
    }

    @Override
    public String[] csrfIgnoredApiEndpoints() {
        return new String[] {
                SecurityInterface.createSubDirectoryPattern(RegistrationController.BASE_ENDPOINT)
        };
    }

}
