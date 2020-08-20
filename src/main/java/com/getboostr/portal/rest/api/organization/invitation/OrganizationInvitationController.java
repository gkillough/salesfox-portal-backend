package com.getboostr.portal.rest.api.organization.invitation;

import com.getboostr.portal.rest.api.organization.common.OrganizationEndpointConstants;
import com.getboostr.portal.rest.api.organization.invitation.model.OrganizationAccountInvitationModel;
import com.getboostr.portal.rest.api.organization.invitation.model.OrganizationAssignableRolesModel;
import com.getboostr.portal.rest.security.authentication.AnonymouslyAccessible;
import com.getboostr.portal.rest.security.authorization.PortalAuthorityConstants;
import com.getboostr.portal.rest.security.common.SecurityInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping(OrganizationEndpointConstants.BASE_ENDPOINT)
public class OrganizationInvitationController implements AnonymouslyAccessible {
    public static final String INVITE_ENDPOINT = "/invite";
    public static final String VALIDATE_INVITE_ENDPOINT = INVITE_ENDPOINT + "/validate";

    private final OrganizationInvitationService organizationInvitationService;

    @Autowired
    public OrganizationInvitationController(OrganizationInvitationService organizationInvitationService) {
        this.organizationInvitationService = organizationInvitationService;
    }

    @GetMapping("/assignable_roles")
    @PreAuthorize(PortalAuthorityConstants.ORGANIZATION_ACCOUNT_OWNER_AUTH_CHECK)
    public OrganizationAssignableRolesModel getAssignableRoles() {
        return organizationInvitationService.getAssignableRoles();
    }

    @PostMapping(INVITE_ENDPOINT)
    @PreAuthorize(PortalAuthorityConstants.ORGANIZATION_ACCOUNT_OWNER_AUTH_CHECK)
    public void sendOrganizationAccountInvitation(@RequestBody OrganizationAccountInvitationModel requestModel) {
        organizationInvitationService.sendOrganizationAccountInvitation(requestModel);
    }

    @GetMapping(VALIDATE_INVITE_ENDPOINT)
    public void validateOrganizationAccountInvitation(HttpServletResponse response, @RequestParam("email") String emailRequestParam, @RequestParam("token") String tokenRequestParam) {
        organizationInvitationService.validateOrganizationAccountInvitation(response, emailRequestParam, tokenRequestParam);
    }

    @Override
    public String[] anonymouslyAccessibleApiEndpoints() {
        return new String[] {
                OrganizationEndpointConstants.BASE_ENDPOINT + VALIDATE_INVITE_ENDPOINT,
                SecurityInterface.createSubDirectoryPattern(OrganizationEndpointConstants.BASE_ENDPOINT + VALIDATE_INVITE_ENDPOINT)
        };
    }

}
