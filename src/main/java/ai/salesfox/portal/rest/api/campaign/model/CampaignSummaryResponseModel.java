package ai.salesfox.portal.rest.api.campaign.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CampaignSummaryResponseModel {
    private LocalDate localDate;
    private Integer recipientCount;

}
