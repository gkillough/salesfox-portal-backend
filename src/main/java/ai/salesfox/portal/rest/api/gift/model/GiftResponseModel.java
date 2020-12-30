package ai.salesfox.portal.rest.api.gift.model;

import ai.salesfox.portal.rest.api.common.model.request.RestrictionModel;
import ai.salesfox.portal.rest.api.user.common.model.UserSummaryModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GiftResponseModel {
    private UUID giftId;
    private UserSummaryModel requestingUser;
    private UUID noteId;
    private UUID itemId;
    private UUID customTextId;
    private UUID customIconId;
    private String mockupImageUrl;
    private GiftTrackingModel tracking;
    private RestrictionModel restriction;

}
