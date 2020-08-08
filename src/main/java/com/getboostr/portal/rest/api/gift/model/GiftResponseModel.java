package com.getboostr.portal.rest.api.gift.model;

import com.getboostr.portal.rest.api.common.model.request.RestrictionModel;
import com.getboostr.portal.rest.api.contact.model.ContactSummaryModel;
import com.getboostr.portal.rest.api.user.common.model.ViewUserModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GiftResponseModel {
    private UUID giftId;
    private ViewUserModel requestingUser;
    private ContactSummaryModel contact;
    private UUID noteId;
    private UUID itemId;
    private UUID customTextId;
    private UUID customIconId;
    private GiftTrackingModel tracking;
    private RestrictionModel restriction;

}
