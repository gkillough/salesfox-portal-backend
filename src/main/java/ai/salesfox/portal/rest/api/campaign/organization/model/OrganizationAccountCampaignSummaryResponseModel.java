package ai.salesfox.portal.rest.api.campaign.organization.model;

import ai.salesfox.portal.database.campaign.view.CampaignDateSummaryView;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationAccountCampaignSummaryResponseModel {
    private LocalDate date;
    private Integer userCount;
    private Integer totalRecipientCount;

    public static OrganizationAccountCampaignSummaryResponseModel fromView(CampaignDateSummaryView view) {
        return new OrganizationAccountCampaignSummaryResponseModel(view.getDate(), view.getUserCount().intValue(), view.getTotalRecipientCount().intValue());
    }

}
