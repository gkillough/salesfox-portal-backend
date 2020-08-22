package ai.salesfox.portal.rest.api.organization.users;

import ai.salesfox.portal.rest.api.organization.common.OrganizationEndpointConstants;
import ai.salesfox.portal.rest.api.organization.users.model.NewAccountOwnerRequestModel;
import ai.salesfox.portal.rest.api.organization.users.model.OrganizationMultiUsersModel;
import ai.salesfox.portal.rest.api.organization.users.model.OrganizationUserAdminViewModel;
import ai.salesfox.portal.rest.api.common.model.request.ActiveStatusPatchModel;
import ai.salesfox.portal.rest.api.common.page.PageMetadata;
import ai.salesfox.portal.rest.api.organization.owner.OrganizationAccountOwnerService;
import ai.salesfox.portal.rest.security.authorization.PortalAuthorityConstants;
import ai.salesfox.portal.rest.api.user.profile.model.UserProfileModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

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
    @PreAuthorize(PortalAuthorityConstants.PORTAL_ADMIN_OR_ORG_ACCT_OWNER_OR_ORG_ACCT_MANAGER_AUTH_CHECK)
    public OrganizationMultiUsersModel getOrganizationAccountUsers(@PathVariable UUID accountId, @RequestParam(defaultValue = PageMetadata.DEFAULT_OFFSET_STRING) Integer offset, @RequestParam(defaultValue = PageMetadata.DEFAULT_LIMIT_STRING) Integer limit) {
        return organizationUsersService.getOrganizationAccountUsers(accountId, offset, limit);
    }

    @GetMapping("/users/{userId}")
    @PreAuthorize(PortalAuthorityConstants.PORTAL_ADMIN_OR_ORG_ACCT_OWNER_OR_ORG_ACCT_MANAGER_AUTH_CHECK)
    public OrganizationUserAdminViewModel getOrganizationAccountUser(@PathVariable UUID accountId, @PathVariable UUID userId) {
        return organizationUsersService.getOrganizationAccountUser(accountId, userId);
    }

    @PatchMapping("/users/{userId}/active")
    @PreAuthorize(PortalAuthorityConstants.PORTAL_ADMIN_OR_ORG_ACCT_OWNER_OR_ORG_ACCT_MANAGER_AUTH_CHECK)
    public void setOrganizationAccountUserActiveStatus(@PathVariable UUID accountId, @PathVariable UUID userId, @RequestBody ActiveStatusPatchModel updateModel) {
        organizationUsersService.setOrganizationAccountUserActiveStatus(accountId, userId, updateModel);
    }

    @PostMapping("/users/{userId}/unlock")
    @PreAuthorize(PortalAuthorityConstants.PORTAL_ADMIN_OR_ORG_ACCT_OWNER_OR_ORG_ACCT_MANAGER_AUTH_CHECK)
    public void unlockOrganizationAccountUser(@PathVariable UUID accountId, @PathVariable UUID userId) {
        organizationUsersService.unlockOrganizationAccountUser(accountId, userId);
    }

    @GetMapping("/account_owner")
    public UserProfileModel getOrganizationAccountOwner(@PathVariable UUID accountId) {
        return organizationAccountOwnerService.getOrganizationAccountOwner(accountId);
    }

    @PostMapping("/account_owner")
    @PreAuthorize(PortalAuthorityConstants.PORTAL_ADMIN_OR_ORG_ACCOUNT_OWNER_AUTH_CHECK)
    public void transferOrganizationAccountOwnership(@PathVariable UUID accountId, @RequestBody NewAccountOwnerRequestModel requestModel) {
        organizationAccountOwnerService.transferOrganizationAccountOwnership(accountId, requestModel);
    }

}
