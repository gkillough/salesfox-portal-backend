package com.usepipeline.portal.web.organization.invitation;

import com.usepipeline.portal.web.organization.OrganizationEndpointConstants;
import com.usepipeline.portal.web.organization.invitation.model.OrganizationAccountInvitationModel;
import com.usepipeline.portal.web.organization.invitation.model.OrganizationAssignableRolesModel;
import com.usepipeline.portal.web.security.authorization.PortalAuthorityConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(OrganizationEndpointConstants.BASE_ENDPOINT)
public class OrganizationInvitationController {
    private OrganizationInvitationService organizationInvitationService;

    @Autowired
    public OrganizationInvitationController(OrganizationInvitationService organizationInvitationService) {
        this.organizationInvitationService = organizationInvitationService;
    }

    @GetMapping("/assignable_roles")
    @PreAuthorize(PortalAuthorityConstants.ORGANIZATION_ACCOUNT_OWNER_AUTH_CHECK)
    public OrganizationAssignableRolesModel getAssignableRoles() {
        return organizationInvitationService.getAssignableRoles();
    }

    @PostMapping("/invite")
    @PreAuthorize(PortalAuthorityConstants.ORGANIZATION_ACCOUNT_OWNER_AUTH_CHECK)
    public void sendOrganizationAccountInvitation(@RequestBody OrganizationAccountInvitationModel requestModel) {
        organizationInvitationService.sendOrganizationAccountInvitation(requestModel);
    }

}
