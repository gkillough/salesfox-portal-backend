package ai.salesfox.portal.rest.api.license.type;

import ai.salesfox.portal.rest.api.common.page.PageRequestValidationUtils;
import ai.salesfox.portal.rest.api.common.page.PagedResponseModel;
import ai.salesfox.portal.rest.api.license.type.model.LicenseTypeCreationRequestModel;
import ai.salesfox.portal.rest.api.license.type.model.LicenseTypeResponseModel;
import ai.salesfox.portal.rest.api.license.type.model.LicenseTypeUpdateRequestModel;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class LicenseTypeService {
    public PagedResponseModel getLicenseTypes(Integer pageOffset, Integer pageLimit, String query) {
        PageRequestValidationUtils.validatePagingParams(pageOffset, pageLimit);
        // TODO implement
        return null;
    }

    public LicenseTypeResponseModel getLicenseType(UUID licenseTypeId) {
        // TODO implement
        return null;
    }

    public LicenseTypeResponseModel createLicenseType(LicenseTypeCreationRequestModel requestModel) {
        // TODO implement
        return null;
    }

    public void updateLicenseType(UUID licenseTypeId, LicenseTypeUpdateRequestModel requestModel) {
        // TODO implement
    }

    public void deleteLicenseType(UUID licenseTypeId) {
        // TODO implement
    }

}
