package ai.salesfox.portal.rest.api.campaign.organization;

import ai.salesfox.portal.rest.api.campaign.CampaignSummaryEndpointService;
import ai.salesfox.portal.rest.api.campaign.organization.model.MultiOrganizationAccountCampaignSummaryResponseModel;
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
    public MultiOrganizationAccountCampaignSummaryResponseModel getOrganizationCampaigns(
            @PathVariable UUID accountId,
            @RequestParam(defaultValue = "30") Integer lookbackDays
    ) {
        return campaignSummaryEndpointService.getOrganizationAccountCampaigns(accountId, lookbackDays);
    }

}
