package ai.salesfox.portal.rest.api.campaign.user.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserCampaignSummaryResponseModel {
    private LocalDate date;
    private Integer recipientCount;

}
