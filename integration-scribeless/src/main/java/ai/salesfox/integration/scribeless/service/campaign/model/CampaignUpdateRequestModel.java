package ai.salesfox.integration.scribeless.service.campaign.model;

import ai.salesfox.integration.scribeless.model.ScribelessAddressModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CampaignUpdateRequestModel {
    private List<ScribelessAddressModel> recipients;

}
