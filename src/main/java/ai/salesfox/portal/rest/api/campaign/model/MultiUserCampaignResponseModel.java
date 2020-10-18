package ai.salesfox.portal.rest.api.campaign.model;

import ai.salesfox.portal.rest.api.common.page.PagedResponseModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class MultiUserCampaignResponseModel extends PagedResponseModel {
    private List<UserCampaignResponseModel> userCampaigns;

    public static MultiUserCampaignResponseModel empty() {
        return new MultiUserCampaignResponseModel(List.of(), Page.empty());
    }

    public MultiUserCampaignResponseModel(List<UserCampaignResponseModel> userCampaigns, Page<?> page) {
        super(page);
        this.userCampaigns = userCampaigns;
    }

}
