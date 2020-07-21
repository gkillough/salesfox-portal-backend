package com.getboostr.portal.rest.customization.icon;

import com.getboostr.portal.common.exception.PortalFileSystemException;
import com.getboostr.portal.database.customization.icon.CustomIconEntity;
import com.getboostr.portal.database.customization.icon.CustomIconFileEntity;
import com.getboostr.portal.database.customization.icon.CustomIconFileRepository;
import com.getboostr.portal.database.customization.icon.CustomIconRepository;
import com.getboostr.portal.rest.image.HttpSafeImageUtility;
import com.getboostr.portal.rest.image.model.ImageResponseModel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
public class CustomIconImageService {
    private CustomIconRepository customIconRepository;
    private CustomIconFileRepository customIconFileRepository;
    private CustomIconAccessService customIconAccessService;
    private HttpSafeImageUtility imageUtility;

    @Autowired
    public CustomIconImageService(CustomIconRepository customIconRepository, CustomIconFileRepository customIconFileRepository, CustomIconAccessService customIconAccessService, HttpSafeImageUtility imageUtility) {
        this.customIconRepository = customIconRepository;
        this.customIconFileRepository = customIconFileRepository;
        this.customIconAccessService = customIconAccessService;
        this.imageUtility = imageUtility;
    }

    public ImageResponseModel getCustomIconImage(UUID customIconId) {
        CustomIconEntity foundCustomIconEntity = customIconRepository.findById(customIconId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        customIconAccessService.validateImageAccess(foundCustomIconEntity);

        // The image is uploaded separately from the main entry, so 404 is appropriate here.
        CustomIconFileEntity foundCustomIconFileEntity = customIconFileRepository.findById(customIconId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        try {
            return imageUtility.getImageResponseModel(foundCustomIconFileEntity::getFileName);
        } catch (PortalFileSystemException e) {
            log.error("There was a problem retrieving custom icon with id [{}]: {}", foundCustomIconFileEntity.getCustomIconId(), e.getMessage());
            log.debug("Icon retrieval error stack trace", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    public void setCustomIconImage(UUID customIconId, MultipartFile iconFile) {
        CustomIconEntity foundCustomIconEntity = customIconRepository.findById(customIconId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        customIconAccessService.validateImageAccess(foundCustomIconEntity);

        // TODO consider restrictions around uploader id

        File savedImageFile = imageUtility.saveImage(iconFile);
        Optional<CustomIconFileEntity> optionalExistingFileEntity = customIconFileRepository.findById(customIconId);
        CustomIconFileEntity customIconFileToSave;
        if (optionalExistingFileEntity.isPresent()) {
            customIconFileToSave = optionalExistingFileEntity.get();
            String oldFileName = customIconFileToSave.getFileName();
            imageUtility.deleteImageByName(oldFileName);
        } else {
            customIconFileToSave = new CustomIconFileEntity(customIconId, null);
        }

        String savedImageFileName = FilenameUtils.getName(savedImageFile.getName());
        customIconFileToSave.setFileName(savedImageFileName);
        customIconFileRepository.save(customIconFileToSave);
    }

}
