package ai.salesfox.portal.rest.api.organization.profile;

import ai.salesfox.portal.rest.api.organization.profile.model.OrganizationAccountProfileModel;
import ai.salesfox.portal.rest.api.organization.profile.model.OrganizationAccountProfileUpdateModel;
import ai.salesfox.portal.rest.api.organization.common.OrganizationEndpointConstants;
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
