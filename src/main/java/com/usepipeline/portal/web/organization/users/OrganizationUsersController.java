package com.usepipeline.portal.web.organization.users;

import com.usepipeline.portal.web.organization.common.OrganizationEndpointConstants;
import com.usepipeline.portal.web.organization.users.model.NewAccountOwnerRequestModel;
import com.usepipeline.portal.web.organization.users.model.OrganizationMultiUsersModel;
import com.usepipeline.portal.web.security.authorization.PortalAuthorityConstants;
import com.usepipeline.portal.web.user.profile.model.UserProfileModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping(OrganizationUsersController.BASE_ENDPOINT)
public class OrganizationUsersController {
    public static final String BASE_ENDPOINT = OrganizationEndpointConstants.ACCOUNT_ENDPOINT + "/{accountId}";

    private OrganizationUsersService organizationUsersService;

    @Autowired
    public OrganizationUsersController(OrganizationUsersService organizationUsersService) {
        this.organizationUsersService = organizationUsersService;
    }

    @GetMapping("/users")
    public OrganizationMultiUsersModel getOrganizationAccountUsers(@PathVariable Long accountId) {
        // TODO implement
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED);
    }

    @GetMapping("/users/{userId}")
    public UserProfileModel getOrganizationAccountUser(@PathVariable Long accountId, @PathVariable Long userId) {
        // TODO implement
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED);
    }

    @GetMapping("/account_owner")
    public UserProfileModel getOrganizationAccountOwner(@PathVariable Long accountId) {
        return organizationUsersService.getOrganizationAccountOwner(accountId);
    }

    @PostMapping("/account_owner")
    @PreAuthorize(PortalAuthorityConstants.PIPELINE_ADMIN_OR_ORG_ACCOUNT_OWNER_AUTH_CHECK)
    public void transferOrganizationAccountOwnership(@PathVariable Long accountId, @RequestBody NewAccountOwnerRequestModel requestModel) {
        organizationUsersService.transferOrganizationAccountOwnership(accountId, requestModel);
    }

}
