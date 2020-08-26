package ai.salesfox.portal.rest.api.gift.scheduling;

import ai.salesfox.portal.common.model.PortalDateModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GiftScheduleRequestModel {
    private PortalDateModel sendDate;

}
