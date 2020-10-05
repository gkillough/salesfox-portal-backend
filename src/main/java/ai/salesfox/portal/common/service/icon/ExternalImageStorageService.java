package ai.salesfox.portal.common.service.icon;

import ai.salesfox.portal.common.enumeration.PortalImageStorageDestination;
import ai.salesfox.portal.common.exception.PortalException;
import org.springframework.web.multipart.MultipartFile;

public interface ExternalImageStorageService {
    String storeImageAndRetrieveUrl(PortalImageStorageDestination destination, MultipartFile multipartFile) throws PortalException;

}
