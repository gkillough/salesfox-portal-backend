package ai.salesfox.portal.rest.api.campaign.organization.model;

import ai.salesfox.portal.rest.api.common.page.PagedResponseModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class MultiOrganizationAccountCampaignSummaryResponseModel extends PagedResponseModel {
    private List<OrganizationAccountCampaignSummaryResponseModel> campaignSummaries;

    public MultiOrganizationAccountCampaignSummaryResponseModel(List<OrganizationAccountCampaignSummaryResponseModel> campaignSummaries, Page<?> page) {
        super(page);
        this.campaignSummaries = campaignSummaries;
    }

}
