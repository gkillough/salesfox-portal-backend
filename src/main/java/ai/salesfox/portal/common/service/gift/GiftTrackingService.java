package ai.salesfox.portal.common.service.gift;

import ai.salesfox.portal.common.enumeration.GiftTrackingStatus;
import ai.salesfox.portal.common.time.PortalDateTimeUtils;
import ai.salesfox.portal.database.account.entity.UserEntity;
import ai.salesfox.portal.database.gift.GiftEntity;
import ai.salesfox.portal.database.gift.tracking.GiftTrackingEntity;
import ai.salesfox.portal.database.gift.tracking.GiftTrackingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GiftTrackingService {
    private final GiftTrackingRepository giftTrackingRepository;

    @Autowired
    public GiftTrackingService(GiftTrackingRepository giftTrackingRepository) {
        this.giftTrackingRepository = giftTrackingRepository;
    }

    public void updateGiftTrackingInfo(GiftEntity gift, UserEntity updatingUser, GiftTrackingStatus status) {
        updateGiftTrackingInfo(gift, updatingUser, status.name());
    }

    public void updateGiftTrackingInfo(GiftEntity gift, UserEntity updatingUser, String status) {
        GiftTrackingEntity giftTrackingToUpdate = gift.getGiftTrackingEntity();
        giftTrackingToUpdate.setStatus(status);
        giftTrackingToUpdate.setUpdatedByUserId(updatingUser.getUserId());
        giftTrackingToUpdate.setDateUpdated(PortalDateTimeUtils.getCurrentDateTime());
        GiftTrackingEntity savedGiftTracking = giftTrackingRepository.save(giftTrackingToUpdate);
        gift.setGiftTrackingEntity(savedGiftTracking);
    }

}
