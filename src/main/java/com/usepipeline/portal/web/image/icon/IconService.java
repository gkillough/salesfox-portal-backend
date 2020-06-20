package com.usepipeline.portal.web.image.icon;

import com.usepipeline.portal.common.exception.PortalFileSystemException;
import com.usepipeline.portal.common.service.icon.LocalIconManager;
import com.usepipeline.portal.database.catalogue.icon.CatalogueItemIconEntity;
import com.usepipeline.portal.database.catalogue.icon.CatalogueItemIconRepository;
import com.usepipeline.portal.web.image.model.ImageResponseModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.awt.image.BufferedImage;
import java.util.UUID;

@Slf4j
@Component
public class IconService {
    private CatalogueItemIconRepository itemIconRepository;
    private LocalIconManager localIconManager;

    @Autowired
    public IconService(CatalogueItemIconRepository itemIconRepository, LocalIconManager localIconManager) {
        this.itemIconRepository = itemIconRepository;
        this.localIconManager = localIconManager;
    }

    public ImageResponseModel getIcon(UUID iconId) {
        CatalogueItemIconEntity foundIcon = itemIconRepository.findById(iconId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        try {
            BufferedImage bufferedIcon = localIconManager.retrieveIcon(foundIcon.getFileName())
                    .orElseThrow(() -> {
                        log.error("Icon with id [{}] not found", iconId);
                        return new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
                    });
            return new ImageResponseModel(bufferedIcon, MediaType.IMAGE_JPEG);
        } catch (PortalFileSystemException e) {
            log.error("There was a problem retrieving icon with id [{}]: {}", iconId, e.getMessage());
            log.debug("Icon retrieval error stack trace", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    public void deleteIcon(UUID iconId) {
        CatalogueItemIconEntity foundIcon = itemIconRepository.findById(iconId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        try {
            boolean wasIconDeletedFromFileSystem = localIconManager.deleteIcon(foundIcon.getFileName());
            if (wasIconDeletedFromFileSystem) {
                itemIconRepository.deleteById(iconId);
                return;
            }
            log.error("Could not delete icon with id [{}] from the file system", iconId);
        } catch (PortalFileSystemException e) {
            log.error("There was a problem deleting the icon file", e);
        }
        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
