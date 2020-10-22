package ai.salesfox.portal.database.campaign.view;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CampaignDateSummaryView {
    private LocalDate date;
    private Long userCount;
    private Long totalRecipientCount;

}
