package com.getboostr.portal.rest.api.customization.icon;

import com.getboostr.portal.database.gift.customization.GiftCustomIconDetailEntity;
import com.getboostr.portal.database.gift.customization.GiftCustomIconDetailRepository;
import com.getboostr.portal.database.gift.tracking.GiftTrackingEntity;
import com.getboostr.portal.database.gift.tracking.GiftTrackingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class CustomIconGiftStatusValidator {
    private final GiftTrackingRepository giftTrackingRepository;
    private final GiftCustomIconDetailRepository giftCustomIconDetailRepository;

    @Autowired
    public CustomIconGiftStatusValidator(GiftTrackingRepository giftTrackingRepository, GiftCustomIconDetailRepository giftCustomIconDetailRepository) {
        this.giftTrackingRepository = giftTrackingRepository;
        this.giftCustomIconDetailRepository = giftCustomIconDetailRepository;
    }

    public void validateCustomIconGiftStatus(UUID customIconId) {
        List<UUID> customIconGiftIds = giftCustomIconDetailRepository.findByCustomIconId(customIconId)
                .stream()
                .map(GiftCustomIconDetailEntity::getGiftId)
                .collect(Collectors.toList());
        if (!customIconGiftIds.isEmpty()) {
            boolean hasCustomIconBeenSubmitted = giftTrackingRepository.findAllById(customIconGiftIds)
                    .stream()
                    .anyMatch(GiftTrackingEntity::isSubmitted);
            if (hasCustomIconBeenSubmitted) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot update a custom icon that has been submitted with a gift");
            }
        }
    }

}
