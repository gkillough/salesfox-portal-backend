package com.getboostr.portal.rest.gift.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GiftResponseModel {
    private UUID giftId;
    private UUID organizationAccountId;
    private UUID requestingUserId;
    private UUID contactId;
    private UUID noteId;
    private UUID itemId;
    private UUID customTextId;
    private UUID customIconId;
    private GiftTrackingModel tracking;

}
