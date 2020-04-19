package com.usepipeline.portal.web.organization.activation;

import com.usepipeline.portal.web.common.model.ActiveStatusPatchModel;
import com.usepipeline.portal.web.organization.OrganizationEndpointConstants;
import org.springframework.beans.factory.annotation.Autowired;
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
    public void setOrganizationAccountActiveStatus(@PathVariable Long accountId, @RequestBody ActiveStatusPatchModel requestModel) {
        organizationActivationService.updateOrganizationAccountActiveStatus(accountId, requestModel);
    }

}
