package ai.salesfox.portal.rest.api.image.icon;

import ai.salesfox.portal.rest.api.image.model.ImageResponseModel;
import ai.salesfox.portal.common.exception.PortalFileSystemException;
import ai.salesfox.portal.common.service.icon.LocalIconManager;
import ai.salesfox.portal.database.catalogue.icon.CatalogueItemIconEntity;
import ai.salesfox.portal.database.catalogue.icon.CatalogueItemIconRepository;
import ai.salesfox.portal.rest.api.image.HttpSafeImageUtility;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Slf4j
@Component
public class CatalogueItemIconService {
    private CatalogueItemIconRepository itemIconRepository;
    private LocalIconManager localIconManager;
    private HttpSafeImageUtility imageUtility;

    @Autowired
    public CatalogueItemIconService(CatalogueItemIconRepository itemIconRepository, LocalIconManager localIconManager, HttpSafeImageUtility imageUtility) {
        this.itemIconRepository = itemIconRepository;
        this.localIconManager = localIconManager;
        this.imageUtility = imageUtility;
    }

    public ImageResponseModel getIcon(UUID iconId) {
        CatalogueItemIconEntity foundIcon = itemIconRepository.findById(iconId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        // TODO check permissions
        try {
            return imageUtility.getImageResponseModel(foundIcon::getFileName);
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
