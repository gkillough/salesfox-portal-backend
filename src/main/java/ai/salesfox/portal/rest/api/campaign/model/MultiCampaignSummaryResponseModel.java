package ai.salesfox.portal.rest.api.campaign.model;

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
public class MultiCampaignSummaryResponseModel extends PagedResponseModel {
    private UserSummaryModel user;
    private List<CampaignSummaryResponseModel> campaignSummaries;

    public static MultiCampaignSummaryResponseModel empty(UserSummaryModel user) {
        return new MultiCampaignSummaryResponseModel(user, List.of(), Page.empty());
    }

    public MultiCampaignSummaryResponseModel(UserSummaryModel user, List<CampaignSummaryResponseModel> campaignSummaries, Page<?> page) {
        super(page);
        this.user = user;
        this.campaignSummaries = campaignSummaries;
    }

}
