package ai.salesfox.portal.rest.api.organization.invitation;

import ai.salesfox.portal.rest.api.common.model.response.ValidationResponseModel;
import ai.salesfox.portal.rest.api.organization.common.OrganizationEndpointConstants;
import ai.salesfox.portal.rest.api.organization.invitation.model.OrganizationAccountInvitationModel;
import ai.salesfox.portal.rest.api.organization.invitation.model.OrganizationAssignableRolesModel;
import ai.salesfox.portal.rest.security.authentication.AnonymouslyAccessible;
import ai.salesfox.portal.rest.security.authorization.PortalAuthorityConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
public class OrganizationInvitationController implements AnonymouslyAccessible {
    public static final String ASSIGNABLE_ROLES_ENDPOINT = OrganizationEndpointConstants.BASE_ENDPOINT + "/assignable_roles";
    public static final String INVITE_ENDPOINT = OrganizationEndpointConstants.BASE_ENDPOINT + "/invite";
    public static final String VALIDATE_INVITE_ENDPOINT = INVITE_ENDPOINT + "/validate";

    private final OrganizationInvitationService organizationInvitationService;

    @Autowired
    public OrganizationInvitationController(OrganizationInvitationService organizationInvitationService) {
        this.organizationInvitationService = organizationInvitationService;
    }

    @GetMapping(ASSIGNABLE_ROLES_ENDPOINT)
    @PreAuthorize(PortalAuthorityConstants.ORGANIZATION_ACCOUNT_OWNER_AUTH_CHECK)
    public OrganizationAssignableRolesModel getAssignableRoles() {
        return organizationInvitationService.getAssignableRoles();
    }

    @PostMapping(INVITE_ENDPOINT)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize(PortalAuthorityConstants.ORGANIZATION_ACCOUNT_OWNER_AUTH_CHECK)
    public void sendOrganizationAccountInvitation(@RequestBody OrganizationAccountInvitationModel requestModel) {
        organizationInvitationService.sendOrganizationAccountInvitation(requestModel);
    }

    @GetMapping(VALIDATE_INVITE_ENDPOINT)
    public ValidationResponseModel validateOrganizationAccountInvitation(@RequestParam("email") String emailRequestParam, @RequestParam("token") String tokenRequestParam) {
        return organizationInvitationService.validateOrganizationAccountInvitation(emailRequestParam, tokenRequestParam);
    }

    @Override
    public String[] anonymouslyAccessibleApiAntMatchers() {
        return new String[] {
                VALIDATE_INVITE_ENDPOINT
        };
    }

}
