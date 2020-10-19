package ai.salesfox.portal.rest.api.campaign;

import ai.salesfox.portal.rest.api.common.page.PageMetadata;
import ai.salesfox.portal.rest.api.organization.common.OrganizationEndpointConstants;
import ai.salesfox.portal.rest.security.authorization.PortalAuthorityConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(OrganizationCampaignController.BASE_ENDPOINT)
public class OrganizationCampaignController {
    public static final String BASE_ENDPOINT = OrganizationEndpointConstants.ACCOUNT_ENDPOINT + "/{accountId}/campaigns";

    private final CampaignSummaryEndpointService campaignSummaryEndpointService;

    @Autowired
    public OrganizationCampaignController(CampaignSummaryEndpointService campaignSummaryEndpointService) {
        this.campaignSummaryEndpointService = campaignSummaryEndpointService;
    }

    @GetMapping
    @PreAuthorize(PortalAuthorityConstants.PORTAL_ADMIN_OR_ORG_ACCT_OWNER_OR_ORG_ACCT_MANAGER_AUTH_CHECK)
    // TODO eventually support startDate/endDate searching
    public Object getOrganizationCampaigns(
            @PathVariable UUID accountId,
            @RequestParam(defaultValue = PageMetadata.DEFAULT_OFFSET_STRING) Integer offset,
            @RequestParam(defaultValue = PageMetadata.DEFAULT_LIMIT_STRING) Integer limit,
            @RequestParam(defaultValue = "30") Integer inNumberOfDays
    ) {
        return campaignSummaryEndpointService.getOrganizationAccountCampaigns(accountId, offset, limit, inNumberOfDays);
    }

}
