package ai.salesfox.portal.rest.api.customization.icon;

import ai.salesfox.portal.common.enumeration.PortalImageStorageDestination;
import ai.salesfox.portal.common.exception.PortalException;
import ai.salesfox.portal.common.service.icon.ExternalImageStorageService;
import ai.salesfox.portal.database.customization.icon.CustomIconEntity;
import ai.salesfox.portal.database.customization.icon.CustomIconRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Slf4j
@Component
public class CustomIconImageService {
    private final CustomIconRepository customIconRepository;
    private final ExternalImageStorageService externalImageStorageService;
    private final CustomIconAccessService customIconAccessService;
    private final CustomIconGiftStatusValidator customIconGiftStatusValidator;

    @Autowired
    public CustomIconImageService(CustomIconRepository customIconRepository, ExternalImageStorageService externalImageStorageService, CustomIconAccessService customIconAccessService, CustomIconGiftStatusValidator customIconGiftStatusValidator) {
        this.customIconRepository = customIconRepository;
        this.externalImageStorageService = externalImageStorageService;
        this.customIconAccessService = customIconAccessService;
        this.customIconGiftStatusValidator = customIconGiftStatusValidator;
    }

    @Transactional
    public void setCustomIconImage(UUID customIconId, MultipartFile iconFile) {
        CustomIconEntity foundCustomIconEntity = customIconRepository.findById(customIconId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        customIconAccessService.validateImageAccess(foundCustomIconEntity);
        customIconGiftStatusValidator.validateCustomIconGiftStatus(customIconId);

        // FIXME validate things about the image

        // TODO consider restrictions around uploader id

        String iconUrl;
        try {
            iconUrl = externalImageStorageService.storeImageAndRetrieveUrl(PortalImageStorageDestination.USER_IMAGES, iconFile);
        } catch (PortalException e) {
            log.error("There was a problem uploading an image", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to upload the image. If this problem persists, please contact support.");
        }

        foundCustomIconEntity.setIconUrl(iconUrl);
        customIconRepository.save(foundCustomIconEntity);
    }

}
