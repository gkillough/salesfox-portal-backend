package com.getboostr.portal.rest.organization.profile;

import com.getboostr.portal.rest.organization.profile.model.OrganizationAccountProfileModel;
import com.getboostr.portal.rest.organization.profile.model.OrganizationAccountProfileUpdateModel;
import com.getboostr.portal.rest.organization.common.OrganizationEndpointConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(OrganizationEndpointConstants.ACCOUNT_ENDPOINT)
public class OrganizationProfileController {
    private OrganizationProfileService organizationProfileService;

    @Autowired
    public OrganizationProfileController(OrganizationProfileService organizationProfileService) {
        this.organizationProfileService = organizationProfileService;
    }

    @GetMapping("/{accountId}/profile")
    public OrganizationAccountProfileModel getOrganizationAccountProfile(@PathVariable UUID accountId) {
        return organizationProfileService.getProfile(accountId);
    }

    @PutMapping("/{accountId}/profile")
    public void updateOrganizationAccountProfile(@PathVariable UUID accountId, @RequestBody OrganizationAccountProfileUpdateModel requestModel) {
        organizationProfileService.updateProfile(accountId, requestModel);
    }

}
