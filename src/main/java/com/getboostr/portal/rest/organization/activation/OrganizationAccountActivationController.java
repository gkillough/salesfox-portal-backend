package com.getboostr.portal.rest.organization.activation;

import com.getboostr.portal.rest.common.model.request.ActiveStatusPatchModel;
import com.getboostr.portal.rest.organization.common.OrganizationEndpointConstants;
import com.getboostr.portal.rest.security.authorization.PortalAuthorityConstants;
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
