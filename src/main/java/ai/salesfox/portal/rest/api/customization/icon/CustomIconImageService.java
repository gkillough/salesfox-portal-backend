package ai.salesfox.portal.rest.api.customization.icon;

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
    private final CustomIconAccessService customIconAccessService;
    private final CustomIconGiftStatusValidator customIconGiftStatusValidator;

    @Autowired
    public CustomIconImageService(CustomIconRepository customIconRepository, CustomIconAccessService customIconAccessService, CustomIconGiftStatusValidator customIconGiftStatusValidator) {
        this.customIconRepository = customIconRepository;
        this.customIconAccessService = customIconAccessService;
        this.customIconGiftStatusValidator = customIconGiftStatusValidator;
    }

    @Transactional
    public void setCustomIconImage(UUID customIconId, MultipartFile iconFile) {
        CustomIconEntity foundCustomIconEntity = customIconRepository.findById(customIconId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        customIconAccessService.validateImageAccess(foundCustomIconEntity);
        customIconGiftStatusValidator.validateCustomIconGiftStatus(customIconId);

        // TODO consider restrictions around uploader id

        // FIXME upload to CDN and retrieve url
        String iconUrl = null;
        foundCustomIconEntity.setIconUrl(iconUrl);

//        File savedImageFile = imageUtility.saveImage(iconFile);
//        Optional<CustomIconFileEntity> optionalExistingFileEntity = customIconFileRepository.findById(customIconId);
//        CustomIconFileEntity customIconFileToSave;
//        if (optionalExistingFileEntity.isPresent()) {
//            customIconFileToSave = optionalExistingFileEntity.get();
//            String oldFileName = customIconFileToSave.getFileName();
//            imageUtility.deleteImageByName(oldFileName);
//        } else {
//            customIconFileToSave = new CustomIconFileEntity(customIconId, null);
//        }
//
//        String savedImageFileName = FilenameUtils.getName(savedImageFile.getName());
//        customIconFileToSave.(savedImageFileName);
//        customIconFileRepository.save(customIconFileToSave);
    }

}
