package ai.salesfox.portal.rest.api.campaign.model;

import ai.salesfox.portal.rest.api.user.common.model.UserSummaryModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserCampaignResponseModel {
    private UserSummaryModel user;
    private LocalDate localDate;
    private Integer recipientCount;

}
