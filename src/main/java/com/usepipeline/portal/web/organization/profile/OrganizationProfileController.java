package com.usepipeline.portal.web.organization.profile;

import com.usepipeline.portal.web.organization.common.OrganizationEndpointConstants;
import com.usepipeline.portal.web.organization.profile.model.OrganizationAccountProfileModel;
import com.usepipeline.portal.web.organization.profile.model.OrganizationAccountProfileUpdateModel;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(OrganizationEndpointConstants.BASE_ENDPOINT)
public class OrganizationProfileController {

    @GetMapping("/account/{accountId}/profile")
    public OrganizationAccountProfileModel getOrganizationAccountProfile(@PathVariable Long accountId) {
        // TODO implement
        return null;
    }

    @PutMapping("/account/{accountId}/profile")
    public void updateOrganizationAccountProfile(@PathVariable Long accountId, @RequestBody OrganizationAccountProfileUpdateModel requestModel) {
        // TODO implement
    }

}
