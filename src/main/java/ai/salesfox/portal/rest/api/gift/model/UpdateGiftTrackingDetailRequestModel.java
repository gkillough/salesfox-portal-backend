package ai.salesfox.portal.rest.api.gift.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateGiftTrackingDetailRequestModel {
    private String distributor;
    private String trackingId;

}
