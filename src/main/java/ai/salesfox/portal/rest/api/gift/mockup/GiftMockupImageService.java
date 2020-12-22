package ai.salesfox.portal.rest.api.gift.mockup;

import ai.salesfox.portal.common.FieldValidationUtils;
import ai.salesfox.portal.common.enumeration.PortalImageStorageDestination;
import ai.salesfox.portal.common.exception.PortalException;
import ai.salesfox.portal.common.service.icon.ExternalImageStorageService;
import ai.salesfox.portal.database.gift.GiftEntity;
import ai.salesfox.portal.database.gift.GiftRepository;
import ai.salesfox.portal.database.gift.mockup.GiftMockupImageEntity;
import ai.salesfox.portal.database.gift.mockup.GiftMockupImageRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
public class GiftMockupImageService {
    private final GiftRepository giftRepository;
    private final GiftMockupImageRepository giftMockupImageRepository;
    private final ExternalImageStorageService externalImageStorageService;

    @Autowired
    public GiftMockupImageService(GiftRepository giftRepository, GiftMockupImageRepository giftMockupImageRepository, ExternalImageStorageService externalImageStorageService) {
        this.giftRepository = giftRepository;
        this.giftMockupImageRepository = giftMockupImageRepository;
        this.externalImageStorageService = externalImageStorageService;
    }

    @Transactional
    public void setMockupImage(UUID giftId, GiftMockupImageRequestModel requestModel) {
        GiftEntity foundGift = giftRepository.findById(giftId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (StringUtils.isBlank(requestModel.getMockupImageUrl())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The field 'mockupImageUrl' cannot be blank");
        } else if (!FieldValidationUtils.isValidUrl(requestModel.getMockupImageUrl(), false)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The provided URL is invalid");
        }

        saveMockupImageFile(foundGift, requestModel.getMockupImageUrl());
    }

    @Transactional
    public void uploadMockupImage(UUID giftId, MultipartFile mockupImageFile) {
        GiftEntity foundGift = giftRepository.findById(giftId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (null == mockupImageFile) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The field 'iconFile' is required");
        }

        String imageUrl;
        try {
            imageUrl = externalImageStorageService.storeImageAndRetrieveUrl(PortalImageStorageDestination.USER_IMAGES, mockupImageFile, true);
        } catch (PortalException e) {
            log.error("There was a problem uploading an icon", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to upload the icon. If this problem persists, please contact support.");
        }
        saveMockupImageFile(foundGift, imageUrl);
    }

    private void saveMockupImageFile(GiftEntity gift, String imageUrl) {
        GiftMockupImageEntity giftMockupImageEntity = Optional.ofNullable(gift.getGiftMockupImageEntity())
                .orElse(new GiftMockupImageEntity(gift.getGiftId(), null));
        giftMockupImageEntity.setImageUrl(imageUrl);
        giftMockupImageRepository.save(giftMockupImageEntity);
    }

}
