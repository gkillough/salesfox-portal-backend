package ai.salesfox.portal.rest.api.organization.activation;

import ai.salesfox.portal.rest.api.common.model.request.ActiveStatusPatchModel;
import ai.salesfox.portal.rest.api.organization.common.OrganizationEndpointConstants;
import ai.salesfox.portal.rest.security.authorization.PortalAuthorityConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(OrganizationEndpointConstants.ACCOUNT_ENDPOINT)
public class OrganizationAccountActivationController {
    private OrganizationActivationService organizationActivationService;

    @Autowired
    public OrganizationAccountActivationController(OrganizationActivationService organizationActivationService) {
        this.organizationActivationService = organizationActivationService;
    }

    @PatchMapping("/{accountId}/active")
    @PreAuthorize(PortalAuthorityConstants.PORTAL_ADMIN_OR_ORG_ACCOUNT_OWNER_AUTH_CHECK)
    public void setOrganizationAccountActiveStatus(@PathVariable UUID accountId, @RequestBody ActiveStatusPatchModel requestModel) {
        organizationActivationService.updateOrganizationAccountActiveStatus(accountId, requestModel);
    }

}
