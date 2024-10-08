package ai.salesfox.portal.rest.api.gift.util;

import ai.salesfox.portal.database.gift.GiftEntity;
import ai.salesfox.portal.database.gift.customization.GiftCustomIconDetailEntity;
import ai.salesfox.portal.database.gift.customization.GiftCustomTextDetailEntity;
import ai.salesfox.portal.database.gift.item.GiftItemDetailEntity;
import ai.salesfox.portal.database.gift.mockup.GiftMockupImageEntity;
import ai.salesfox.portal.database.gift.note.GiftNoteDetailEntity;
import ai.salesfox.portal.database.gift.restriction.GiftOrgAccountRestrictionEntity;
import ai.salesfox.portal.database.gift.restriction.GiftUserRestrictionEntity;
import ai.salesfox.portal.database.gift.tracking.GiftTrackingEntity;
import ai.salesfox.portal.rest.api.common.model.request.RestrictionModel;
import ai.salesfox.portal.rest.api.gift.model.GiftResponseModel;
import ai.salesfox.portal.rest.api.gift.model.GiftTrackingModel;
import ai.salesfox.portal.rest.api.user.common.model.UserSummaryModel;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

public class GiftResponseModelUtils {
    public static GiftResponseModel convertToResponseModel(GiftEntity gift) {
        UserSummaryModel requestingUserModel = UserSummaryModel.fromEntity(gift.getRequestingUserEntity());

        UUID noteId = extractAttributeOrNull(gift.getGiftNoteDetailEntity(), GiftNoteDetailEntity::getNoteId);
        UUID itemId = extractAttributeOrNull(gift.getGiftItemDetailEntity(), GiftItemDetailEntity::getItemId);
        UUID customIconId = extractAttributeOrNull(gift.getGiftCustomIconDetailEntity(), GiftCustomIconDetailEntity::getCustomIconId);
        UUID customTextId = extractAttributeOrNull(gift.getGiftCustomTextDetailEntity(), GiftCustomTextDetailEntity::getCustomTextId);
        String mockupImageUrl = extractAttributeOrNull(gift.getGiftMockupImageEntity(), GiftMockupImageEntity::getImageUrl);

        GiftTrackingModel trackingModel = createTrackingModel(gift.getGiftTrackingEntity());

        RestrictionModel restrictionModel = new RestrictionModel();
        Optional.ofNullable(gift.getGiftOrgAccountRestrictionEntity()).map(GiftOrgAccountRestrictionEntity::getOrgAccountId).ifPresent(restrictionModel::setOrganizationAccountId);
        Optional.ofNullable(gift.getGiftUserRestrictionEntity()).map(GiftUserRestrictionEntity::getUserId).ifPresent(restrictionModel::setUserId);
        return new GiftResponseModel(gift.getGiftId(), requestingUserModel, noteId, itemId, customTextId, customIconId, mockupImageUrl, trackingModel, restrictionModel);
    }

    public static GiftTrackingModel createTrackingModel(GiftTrackingEntity giftTracking) {
        String distributor = null;
        String trackingNumber = null;
        if (giftTracking.getGiftTrackingDetailEntity() != null) {
            distributor = giftTracking.getGiftTrackingDetailEntity().getDistributor();
            trackingNumber = giftTracking.getGiftTrackingDetailEntity().getTrackingNumber();
        }

        UserSummaryModel updatedByUser = null;
        if (null != giftTracking.getUpdatedByUserEntity()) {
            updatedByUser = UserSummaryModel.fromEntity(giftTracking.getUpdatedByUserEntity());
        }
        return new GiftTrackingModel(giftTracking.getStatus(), distributor, trackingNumber, updatedByUser, giftTracking.getDateCreated(), giftTracking.getDateUpdated());
    }

    private static <T, U> U extractAttributeOrNull(T container, Function<T, U> extractor) {
        return Optional.ofNullable(container).map(extractor).orElse(null);
    }

}
