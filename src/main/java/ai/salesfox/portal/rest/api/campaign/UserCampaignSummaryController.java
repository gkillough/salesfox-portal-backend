package ai.salesfox.portal.rest.api.campaign;

import ai.salesfox.portal.rest.api.campaign.model.MultiCampaignSummaryResponseModel;
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
    public MultiCampaignSummaryResponseModel getUserCampaignSummaries(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = PageMetadata.DEFAULT_OFFSET_STRING) Integer offset,
            @RequestParam(defaultValue = PageMetadata.DEFAULT_LIMIT_STRING) Integer limit,
            @RequestParam(defaultValue = "30") Integer inNumberOfDays
    ) {
        return campaignSummaryEndpointService.getUserCampaignSummaries(userId, offset, limit, inNumberOfDays);
    }

}
