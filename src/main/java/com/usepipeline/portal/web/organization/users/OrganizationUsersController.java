package com.usepipeline.portal.web.organization.users;

import com.usepipeline.portal.web.common.model.request.ActiveStatusPatchModel;
import com.usepipeline.portal.web.common.page.PageMetadata;
import com.usepipeline.portal.web.organization.common.OrganizationEndpointConstants;
import com.usepipeline.portal.web.organization.owner.OrganizationAccountOwnerService;
import com.usepipeline.portal.web.organization.users.model.NewAccountOwnerRequestModel;
import com.usepipeline.portal.web.organization.users.model.OrganizationMultiUsersModel;
import com.usepipeline.portal.web.organization.users.model.OrganizationUserAdminViewModel;
import com.usepipeline.portal.web.security.authorization.PortalAuthorityConstants;
import com.usepipeline.portal.web.user.profile.model.UserProfileModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(OrganizationUsersController.BASE_ENDPOINT)
public class OrganizationUsersController {
    public static final String BASE_ENDPOINT = OrganizationEndpointConstants.ACCOUNT_ENDPOINT + "/{accountId}";

    private OrganizationUsersService organizationUsersService;
    private OrganizationAccountOwnerService organizationAccountOwnerService;

    @Autowired
    public OrganizationUsersController(OrganizationUsersService organizationUsersService, OrganizationAccountOwnerService organizationAccountOwnerService) {
        this.organizationUsersService = organizationUsersService;
        this.organizationAccountOwnerService = organizationAccountOwnerService;
    }

    @GetMapping("/users")
    @PreAuthorize(PortalAuthorityConstants.PIPELINE_ADMIN_OR_ORG_ACCT_OWNER_OR_ORG_ACCT_MANAGER_AUTH_CHECK)
    public OrganizationMultiUsersModel getOrganizationAccountUsers(@PathVariable Long accountId, @RequestParam(defaultValue = PageMetadata.DEFAULT_OFFSET_STRING) Integer offset, @RequestParam(defaultValue = PageMetadata.DEFAULT_LIMIT_STRING) Integer limit) {
        return organizationUsersService.getOrganizationAccountUsers(accountId, offset, limit);
    }

    @GetMapping("/users/{userId}")
    @PreAuthorize(PortalAuthorityConstants.PIPELINE_ADMIN_OR_ORG_ACCT_OWNER_OR_ORG_ACCT_MANAGER_AUTH_CHECK)
    public OrganizationUserAdminViewModel getOrganizationAccountUser(@PathVariable Long accountId, @PathVariable Long userId) {
        return organizationUsersService.getOrganizationAccountUser(accountId, userId);
    }

    @PatchMapping("/users/{userId}/active")
    @PreAuthorize(PortalAuthorityConstants.PIPELINE_ADMIN_OR_ORG_ACCT_OWNER_OR_ORG_ACCT_MANAGER_AUTH_CHECK)
    public void setOrganizationAccountUserActiveStatus(@PathVariable Long accountId, @PathVariable Long userId, @RequestBody ActiveStatusPatchModel updateModel) {
        organizationUsersService.setOrganizationAccountUserActiveStatus(accountId, userId, updateModel);
    }

    @PostMapping("/users/{userId}/unlock")
    @PreAuthorize(PortalAuthorityConstants.PIPELINE_ADMIN_OR_ORG_ACCT_OWNER_OR_ORG_ACCT_MANAGER_AUTH_CHECK)
    public void unlockOrganizationAccountUser(@PathVariable Long accountId, @PathVariable Long userId) {
        organizationUsersService.unlockOrganizationAccountUser(accountId, userId);
    }

    @GetMapping("/account_owner")
    public UserProfileModel getOrganizationAccountOwner(@PathVariable Long accountId) {
        return organizationAccountOwnerService.getOrganizationAccountOwner(accountId);
    }

    @PostMapping("/account_owner")
    @PreAuthorize(PortalAuthorityConstants.PIPELINE_ADMIN_OR_ORG_ACCOUNT_OWNER_AUTH_CHECK)
    public void transferOrganizationAccountOwnership(@PathVariable Long accountId, @RequestBody NewAccountOwnerRequestModel requestModel) {
        organizationAccountOwnerService.transferOrganizationAccountOwnership(accountId, requestModel);
    }

}
