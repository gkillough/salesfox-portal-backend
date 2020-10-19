package ai.salesfox.portal.rest.api.campaign.organization.model;

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

}
