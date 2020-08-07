package com.getboostr.portal.rest.api.gift.util;

import com.getboostr.portal.common.enumeration.GiftTrackingStatus;
import com.getboostr.portal.database.gift.GiftEntity;
import com.getboostr.portal.database.gift.customization.GiftCustomIconDetailEntity;
import com.getboostr.portal.database.gift.customization.GiftCustomTextDetailEntity;
import com.getboostr.portal.database.gift.item.GiftItemDetailEntity;
import com.getboostr.portal.database.gift.note.GiftNoteDetailEntity;
import com.getboostr.portal.database.gift.tracking.GiftTrackingEntity;
import com.getboostr.portal.rest.api.gift.model.GiftResponseModel;
import com.getboostr.portal.rest.api.gift.model.GiftTrackingModel;
import org.springframework.lang.Nullable;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

public class GiftResponseModelUtils {
    public static GiftResponseModel convertToResponseModel(GiftEntity gift) {
        UUID noteId = extractDetailIdOrNull(gift.getGiftNoteDetailEntity(), GiftNoteDetailEntity::getNoteId);
        UUID itemId = extractDetailIdOrNull(gift.getGiftItemDetailEntity(), GiftItemDetailEntity::getItemId);
        UUID customIconId = extractDetailIdOrNull(gift.getGiftCustomIconDetailEntity(), GiftCustomIconDetailEntity::getCustomIconId);
        UUID customTextId = extractDetailIdOrNull(gift.getGiftCustomTextDetailEntity(), GiftCustomTextDetailEntity::getCustomTextId);
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

    private static <E> UUID extractDetailIdOrNull(E detailEntity, Function<E, UUID> getter) {
        return Optional.ofNullable(detailEntity).map(getter).orElse(null);
    }

}
