package com.usepipeline.portal.web.gift.util;

import com.usepipeline.portal.common.enumeration.GiftTrackingStatus;
import com.usepipeline.portal.database.gift.GiftEntity;
import com.usepipeline.portal.database.gift.tracking.GiftTrackingEntity;
import com.usepipeline.portal.web.gift.model.GiftResponseModel;
import com.usepipeline.portal.web.gift.model.GiftTrackingModel;
import org.springframework.lang.Nullable;

import java.time.OffsetDateTime;
import java.util.UUID;

public class GiftResponseModelUtils {
    public static GiftResponseModel convertToResponseModel(GiftEntity gift) {
        UUID noteId = gift.getGiftNoteDetailEntity() != null ? gift.getGiftNoteDetailEntity().getNoteId() : null;
        UUID itemId = gift.getGiftItemDetailEntity() != null ? gift.getGiftItemDetailEntity().getItemId() : null;
        UUID customTextId = null;
        UUID customIconId = null;
        if (gift.getGiftCustomizationDetailEntity() != null) {
            customTextId = gift.getGiftCustomizationDetailEntity().getCustomTextId();
            customIconId = gift.getGiftCustomizationDetailEntity().getCustomIconId();
        }
        GiftTrackingModel trackingModel = createTrackingModel(gift.getGiftTrackingEntity());
        return new GiftResponseModel(gift.getGiftId(), gift.getOrganizationAccountId(), gift.getRequestingUserId(), gift.getContactId(), noteId, itemId, customTextId, customIconId, trackingModel);
    }

    public static GiftTrackingModel createTrackingModel(@Nullable GiftTrackingEntity giftTracking) {
        String status = GiftTrackingStatus.DRAFT.name();
        UUID updatedByUserId = null;
        OffsetDateTime dateSubmitted = null;
        OffsetDateTime dateUpdated = null;
        String distributor = null;
        String trackingNumber = null;
        if (giftTracking != null) {
            status = giftTracking.getStatus();
            updatedByUserId = giftTracking.getUpdatedByUserId();
            dateSubmitted = giftTracking.getDateSubmitted();
            dateUpdated = giftTracking.getDateUpdated();
            if (giftTracking.getGiftTrackingDetailEntity() != null) {
                distributor = giftTracking.getGiftTrackingDetailEntity().getDistributor();
                trackingNumber = giftTracking.getGiftTrackingDetailEntity().getTrackingNumber();
            }
        }
        return new GiftTrackingModel(status, distributor, trackingNumber, updatedByUserId, dateSubmitted, dateUpdated);
    }

}
