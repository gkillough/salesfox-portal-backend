package com.getboostr.portal.web.organization.invitation;

import com.getboostr.portal.web.organization.invitation.model.OrganizationAccountInvitationModel;
import com.getboostr.portal.web.organization.invitation.model.OrganizationAssignableRolesModel;
import com.getboostr.portal.web.organization.common.OrganizationEndpointConstants;
import com.getboostr.portal.web.security.authentication.AnonymouslyAccessible;
import com.getboostr.portal.web.security.authorization.PortalAuthorityConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping(OrganizationEndpointConstants.BASE_ENDPOINT)
public class OrganizationInvitationController implements AnonymouslyAccessible {
    public static final String INVITE_ENDPOINT = "/invite";
    public static final String VALIDATE_INVITE_ENDPOINT = INVITE_ENDPOINT + "/validate";

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
    public String[] allowedEndpointAntMatchers() {
        return new String[]{
                OrganizationEndpointConstants.BASE_ENDPOINT + VALIDATE_INVITE_ENDPOINT,
                createSubDirectoryPattern(OrganizationEndpointConstants.BASE_ENDPOINT + VALIDATE_INVITE_ENDPOINT)
        };
    }

}
