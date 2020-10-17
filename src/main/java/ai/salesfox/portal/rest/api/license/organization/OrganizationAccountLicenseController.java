package ai.salesfox.portal.rest.api.license.organization;

import ai.salesfox.portal.rest.api.license.organization.model.OrganizationAccountLicenseResponseModel;
import ai.salesfox.portal.rest.api.license.organization.model.OrganizationAccountLicenseTypeUpdateRequestModel;
import ai.salesfox.portal.rest.api.organization.common.OrganizationEndpointConstants;
import ai.salesfox.portal.rest.security.authorization.PortalAuthorityConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(OrganizationAccountLicenseController.BASE_ENDPOINT)
public class OrganizationAccountLicenseController {
    public static final String BASE_ENDPOINT = OrganizationEndpointConstants.ACCOUNT_ENDPOINT + "/{accountId}/license";

    private final OrganizationAccountLicenseService organizationAccountLicenseService;

    @Autowired
    public OrganizationAccountLicenseController(OrganizationAccountLicenseService organizationAccountLicenseService) {
        this.organizationAccountLicenseService = organizationAccountLicenseService;
    }

    @GetMapping
    public OrganizationAccountLicenseResponseModel getLicense(@PathVariable UUID accountId) {
        return organizationAccountLicenseService.getLicense(accountId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize(PortalAuthorityConstants.PORTAL_ADMIN_OR_ORG_ACCOUNT_OWNER_AUTH_CHECK)
    public void updateLicense(@PathVariable UUID accountId, OrganizationAccountLicenseTypeUpdateRequestModel requestModel) {
        organizationAccountLicenseService.updateLicense(accountId, requestModel);
    }

}
