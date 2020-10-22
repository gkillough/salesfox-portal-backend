package ai.salesfox.portal.rest.api.campaign.user.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserCampaignBillingPeriodSummaryModel {
    private Integer campaignsSent;
    private Integer campaignsAllowed;

}
