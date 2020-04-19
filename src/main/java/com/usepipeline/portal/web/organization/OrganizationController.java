package com.usepipeline.portal.web.organization;

import com.usepipeline.portal.web.organization.common.OrganizationEndpointConstants;
import com.usepipeline.portal.web.organization.model.OrganizationAccountModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(OrganizationEndpointConstants.BASE_ENDPOINT)
public class OrganizationController {
    private OrganizationService organizationService;

    @Autowired
    public OrganizationController(OrganizationService organizationService) {
        this.organizationService = organizationService;
    }

    @GetMapping("/current_account")
    public OrganizationAccountModel getOrganizationAccountInfo() {
        return organizationService.getOrganizationAccount();
    }

}
