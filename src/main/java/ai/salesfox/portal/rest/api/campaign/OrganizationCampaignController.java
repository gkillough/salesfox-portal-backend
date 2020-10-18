package ai.salesfox.portal.rest.api.campaign;

import ai.salesfox.portal.rest.api.common.page.PageMetadata;
import ai.salesfox.portal.rest.api.organization.common.OrganizationEndpointConstants;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(OrganizationCampaignController.BASE_ENDPOINT)
public class OrganizationCampaignController {
    public static final String BASE_ENDPOINT = OrganizationEndpointConstants.ACCOUNT_ENDPOINT + "/{accountId}/campaigns";

    @GetMapping
    // TODO eventually support startDate/endDate searching
    public Object getOrganizationCampaigns(
            @RequestParam(defaultValue = PageMetadata.DEFAULT_OFFSET_STRING) Integer offset,
            @RequestParam(defaultValue = PageMetadata.DEFAULT_LIMIT_STRING) Integer limit,
            @RequestParam(defaultValue = "30") Integer inNumberOfDays
    ) {
        // FIXME implement
        return null;
    }

}
