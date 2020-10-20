package ai.salesfox.portal.rest.api.campaign.user.model;

import ai.salesfox.portal.rest.api.common.page.PagedResponseModel;
import ai.salesfox.portal.rest.api.user.common.model.UserSummaryModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class MultiUserCampaignSummaryResponseModel extends PagedResponseModel {
    private UserSummaryModel user;
    private List<UserCampaignSummaryResponseModel> campaignSummaries;

    public static MultiUserCampaignSummaryResponseModel empty(UserSummaryModel user) {
        return new MultiUserCampaignSummaryResponseModel(user, List.of(), Page.empty());
    }

    public MultiUserCampaignSummaryResponseModel(UserSummaryModel user, List<UserCampaignSummaryResponseModel> campaignSummaries, Page<?> page) {
        super(page);
        this.user = user;
        this.campaignSummaries = campaignSummaries;
    }

}
