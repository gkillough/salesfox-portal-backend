package ai.salesfox.portal.rest.api.license.organization;

import ai.salesfox.portal.rest.api.organization.common.OrganizationEndpointConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(OrganizationEndpointConstants.ACCOUNT_ENDPOINT)
public class OrganizationAccountLicenseController {
    public static final String BASE_ENDPOINT = OrganizationEndpointConstants.ACCOUNT_ENDPOINT + "{accountId}/license";

    private final OrganizationAccountLicenseService organizationAccountLicenseService;

    @Autowired
    public OrganizationAccountLicenseController(OrganizationAccountLicenseService organizationAccountLicenseService) {
        this.organizationAccountLicenseService = organizationAccountLicenseService;
    }

    @GetMapping
    public Object getLicense(@PathVariable UUID accountId) {
        return organizationAccountLicenseService.getLicense(accountId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateLicense(@PathVariable UUID accountId, Object requestModel) {
        organizationAccountLicenseService.updateLicense(accountId, requestModel);
    }

}
