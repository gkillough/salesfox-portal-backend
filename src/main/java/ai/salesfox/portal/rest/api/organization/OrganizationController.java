package ai.salesfox.portal.rest.api.organization;

import ai.salesfox.portal.rest.api.common.page.PageMetadata;
import ai.salesfox.portal.rest.api.organization.common.OrganizationEndpointConstants;
import ai.salesfox.portal.rest.api.organization.common.model.MultiOrganizationAccountModel;
import ai.salesfox.portal.rest.api.organization.common.model.OrganizationAccountSummaryModel;
import ai.salesfox.portal.rest.security.authorization.PortalAuthorityConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OrganizationController {
    private final OrganizationService organizationService;

    @Autowired
    public OrganizationController(OrganizationService organizationService) {
        this.organizationService = organizationService;
    }

    @GetMapping(OrganizationEndpointConstants.BASE_ENDPOINT + "/current_account")
    public OrganizationAccountSummaryModel getOrganizationAccountInfo() {
        return organizationService.getOrganizationAccount();
    }

    @GetMapping(OrganizationEndpointConstants.ACCOUNTS_ENDPOINT)
    @PreAuthorize(PortalAuthorityConstants.PORTAL_ADMIN_AUTH_CHECK)
    public MultiOrganizationAccountModel getOrganizationAccounts(
            @RequestParam(defaultValue = PageMetadata.DEFAULT_OFFSET_STRING) Integer offset,
            @RequestParam(defaultValue = PageMetadata.DEFAULT_LIMIT_STRING) Integer limit,
            @RequestParam(required = false) String query
    ) {
        return organizationService.getOrganizationAccounts(offset, limit, query);
    }

}
