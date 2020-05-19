package com.usepipeline.portal.web.organization.activation;

import com.usepipeline.portal.web.common.model.request.ActiveStatusPatchModel;
import com.usepipeline.portal.web.organization.common.OrganizationEndpointConstants;
import com.usepipeline.portal.web.security.authorization.PortalAuthorityConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(OrganizationEndpointConstants.ACCOUNT_ENDPOINT)
public class OrganizationAccountActivationController {
    private OrganizationActivationService organizationActivationService;

    @Autowired
    public OrganizationAccountActivationController(OrganizationActivationService organizationActivationService) {
        this.organizationActivationService = organizationActivationService;
    }

    @PatchMapping("/{accountId}/active")
    @PreAuthorize(PortalAuthorityConstants.PIPELINE_ADMIN_OR_ORG_ACCOUNT_OWNER_AUTH_CHECK)
    public void setOrganizationAccountActiveStatus(@PathVariable Long accountId, @RequestBody ActiveStatusPatchModel requestModel) {
        organizationActivationService.updateOrganizationAccountActiveStatus(accountId, requestModel);
    }

}
