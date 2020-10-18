package ai.salesfox.portal.rest.api.campaign;

import ai.salesfox.portal.rest.api.common.page.PageMetadata;
import ai.salesfox.portal.rest.api.organization.common.OrganizationEndpointConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(OrganizationCampaignController.BASE_ENDPOINT)
public class OrganizationCampaignController {
    public static final String BASE_ENDPOINT = OrganizationEndpointConstants.ACCOUNT_ENDPOINT + "/{accountId}/campaigns";

    private final CampaignStatisticsEndpointService campaignStatisticsEndpointService;

    @Autowired
    public OrganizationCampaignController(CampaignStatisticsEndpointService campaignStatisticsEndpointService) {
        this.campaignStatisticsEndpointService = campaignStatisticsEndpointService;
    }

    @GetMapping
    // TODO eventually support startDate/endDate searching
    public Object getOrganizationCampaigns(
            @PathVariable UUID accountId,
            @RequestParam(defaultValue = PageMetadata.DEFAULT_OFFSET_STRING) Integer offset,
            @RequestParam(defaultValue = PageMetadata.DEFAULT_LIMIT_STRING) Integer limit,
            @RequestParam(defaultValue = "30") Integer inNumberOfDays
    ) {
        return campaignStatisticsEndpointService.getOrganizationAccountCampaigns(accountId, offset, limit, inNumberOfDays);
    }

}
