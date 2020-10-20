package ai.salesfox.portal.rest.api.campaign.user;

import ai.salesfox.portal.rest.api.campaign.CampaignSummaryEndpointService;
import ai.salesfox.portal.rest.api.campaign.user.model.MultiUserCampaignSummaryResponseModel;
import ai.salesfox.portal.rest.api.campaign.user.model.UserCampaignBillingPeriodSummaryModel;
import ai.salesfox.portal.rest.api.common.page.PageMetadata;
import ai.salesfox.portal.rest.api.user.UserEndpointConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(UserCampaignSummaryController.BASE_ENDPOINT)
public class UserCampaignSummaryController {
    public static final String BASE_ENDPOINT = UserEndpointConstants.BASE_ENDPOINT + "/{userId}/campaigns";

    private final CampaignSummaryEndpointService campaignSummaryEndpointService;

    @Autowired
    public UserCampaignSummaryController(CampaignSummaryEndpointService campaignSummaryEndpointService) {
        this.campaignSummaryEndpointService = campaignSummaryEndpointService;
    }

    @GetMapping
    // TODO eventually support startDate/endDate searching
    public MultiUserCampaignSummaryResponseModel getUserCampaignSummaries(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = PageMetadata.DEFAULT_OFFSET_STRING) Integer offset,
            @RequestParam(defaultValue = PageMetadata.DEFAULT_LIMIT_STRING) Integer limit,
            @RequestParam(defaultValue = "30") Integer lookbackDays
    ) {
        return campaignSummaryEndpointService.getUserCampaignSummaries(userId, offset, limit, lookbackDays);
    }

    @GetMapping("/billing_period")
    public UserCampaignBillingPeriodSummaryModel getUserCampaignSummariesForBillingPeriod(@PathVariable UUID userId) {
        return campaignSummaryEndpointService.getUserCampaignSummariesForBillingPeriod(userId);
    }

}
