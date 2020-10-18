package ai.salesfox.portal.rest.api.campaign;

import ai.salesfox.portal.rest.api.campaign.model.MultiUserCampaignResponseModel;
import ai.salesfox.portal.rest.api.common.page.PageMetadata;
import ai.salesfox.portal.rest.api.user.UserEndpointConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(UserCampaignController.BASE_ENDPOINT)
public class UserCampaignController {
    public static final String BASE_ENDPOINT = UserEndpointConstants.BASE_ENDPOINT + "/{userId}/campaigns";

    private final CampaignStatisticsEndpointService campaignStatisticsEndpointService;

    @Autowired
    public UserCampaignController(CampaignStatisticsEndpointService campaignStatisticsEndpointService) {
        this.campaignStatisticsEndpointService = campaignStatisticsEndpointService;
    }

    @GetMapping
    // TODO eventually support startDate/endDate searching
    public MultiUserCampaignResponseModel getUserCampaigns(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = PageMetadata.DEFAULT_OFFSET_STRING) Integer offset,
            @RequestParam(defaultValue = PageMetadata.DEFAULT_LIMIT_STRING) Integer limit,
            @RequestParam(defaultValue = "30") Integer inNumberOfDays
    ) {
        return campaignStatisticsEndpointService.getUserCampaigns(userId, offset, limit, inNumberOfDays);
    }

}
