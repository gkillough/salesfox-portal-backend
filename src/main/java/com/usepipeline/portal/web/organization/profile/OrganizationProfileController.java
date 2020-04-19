package com.usepipeline.portal.web.organization.profile;

import com.usepipeline.portal.web.organization.common.OrganizationEndpointConstants;
import com.usepipeline.portal.web.organization.profile.model.OrganizationAccountProfileModel;
import com.usepipeline.portal.web.organization.profile.model.OrganizationAccountProfileUpdateModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(OrganizationEndpointConstants.BASE_ENDPOINT)
public class OrganizationProfileController {
    private OrganizationProfileService organizationProfileService;

    @Autowired
    public OrganizationProfileController(OrganizationProfileService organizationProfileService) {
        this.organizationProfileService = organizationProfileService;
    }

    @GetMapping("/account/{accountId}/profile")
    public OrganizationAccountProfileModel getOrganizationAccountProfile(@PathVariable Long accountId) {
        return organizationProfileService.getProfile(accountId);
    }

    @PutMapping("/account/{accountId}/profile")
    public void updateOrganizationAccountProfile(@PathVariable Long accountId, @RequestBody OrganizationAccountProfileUpdateModel requestModel) {
        organizationProfileService.updateProfile(accountId, requestModel);
    }

}
