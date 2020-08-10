package com.getboostr.portal.rest.api.gift.util;

import com.getboostr.portal.common.enumeration.GiftTrackingStatus;
import com.getboostr.portal.database.gift.GiftEntity;
import com.getboostr.portal.database.gift.customization.GiftCustomIconDetailEntity;
import com.getboostr.portal.database.gift.customization.GiftCustomTextDetailEntity;
import com.getboostr.portal.database.gift.item.GiftItemDetailEntity;
import com.getboostr.portal.database.gift.note.GiftNoteDetailEntity;
import com.getboostr.portal.database.gift.restriction.GiftOrgAccountRestrictionEntity;
import com.getboostr.portal.database.gift.restriction.GiftUserRestrictionEntity;
import com.getboostr.portal.database.gift.tracking.GiftTrackingEntity;
import com.getboostr.portal.rest.api.common.model.request.RestrictionModel;
import com.getboostr.portal.rest.api.contact.model.ContactSummaryModel;
import com.getboostr.portal.rest.api.gift.model.GiftResponseModel;
import com.getboostr.portal.rest.api.gift.model.GiftTrackingModel;
import com.getboostr.portal.rest.api.user.common.model.ViewUserModel;
import org.springframework.lang.Nullable;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

public class GiftResponseModelUtils {
    public static GiftResponseModel convertToResponseModel(GiftEntity gift) {
        ViewUserModel requestingUserModel = ViewUserModel.fromEntity(gift.getRequestingUserEntity());
        ContactSummaryModel contactModel = ContactSummaryModel.fromEntity(gift.getContactEntity());

        UUID noteId = extractDetailIdOrNull(gift.getGiftNoteDetailEntity(), GiftNoteDetailEntity::getNoteId);
        UUID itemId = extractDetailIdOrNull(gift.getGiftItemDetailEntity(), GiftItemDetailEntity::getItemId);
        UUID customIconId = extractDetailIdOrNull(gift.getGiftCustomIconDetailEntity(), GiftCustomIconDetailEntity::getCustomIconId);
        UUID customTextId = extractDetailIdOrNull(gift.getGiftCustomTextDetailEntity(), GiftCustomTextDetailEntity::getCustomTextId);

        GiftTrackingModel trackingModel = createTrackingModel(gift.getGiftTrackingEntity());

        RestrictionModel restrictionModel = new RestrictionModel();
        Optional.ofNullable(gift.getGiftOrgAccountRestrictionEntity()).map(GiftOrgAccountRestrictionEntity::getOrgAccountId).ifPresent(restrictionModel::setOrganizationAccountId);
        Optional.ofNullable(gift.getGiftUserRestrictionEntity()).map(GiftUserRestrictionEntity::getUserId).ifPresent(restrictionModel::setUserId);
        return new GiftResponseModel(gift.getGiftId(), requestingUserModel, contactModel, noteId, itemId, customTextId, customIconId, trackingModel, restrictionModel);
    }

    public static GiftTrackingModel createTrackingModel(@Nullable GiftTrackingEntity giftTracking) {
        String status = GiftTrackingStatus.DRAFT.name();
        UUID updatedByUserId = null;
        OffsetDateTime dateCreated = null;
        OffsetDateTime dateUpdated = null;
        String distributor = null;
        String trackingNumber = null;
        if (giftTracking != null) {
            status = giftTracking.getStatus();
            updatedByUserId = giftTracking.getUpdatedByUserId();
            dateCreated = giftTracking.getDateCreated();
            dateUpdated = giftTracking.getDateUpdated();
            if (giftTracking.getGiftTrackingDetailEntity() != null) {
                distributor = giftTracking.getGiftTrackingDetailEntity().getDistributor();
                trackingNumber = giftTracking.getGiftTrackingDetailEntity().getTrackingNumber();
            }
        }
        return new GiftTrackingModel(status, distributor, trackingNumber, updatedByUserId, dateCreated, dateUpdated);
    }

    private static <E> UUID extractDetailIdOrNull(E detailEntity, Function<E, UUID> getter) {
        return Optional.ofNullable(detailEntity).map(getter).orElse(null);
    }

}
