package ai.salesfox.portal.rest.api.license.type;

import ai.salesfox.portal.database.license.LicenseTypeEntity;
import ai.salesfox.portal.database.license.LicenseTypeRepository;
import ai.salesfox.portal.database.license.OrganizationAccountLicenseRepository;
import ai.salesfox.portal.rest.api.common.page.PageRequestValidationUtils;
import ai.salesfox.portal.rest.api.common.page.PagedResponseModel;
import ai.salesfox.portal.rest.api.license.type.model.AbstractLicenseTypeRequestModel;
import ai.salesfox.portal.rest.api.license.type.model.LicenseTypeCreationRequestModel;
import ai.salesfox.portal.rest.api.license.type.model.LicenseTypeResponseModel;
import ai.salesfox.portal.rest.api.license.type.model.LicenseTypeUpdateRequestModel;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import javax.annotation.Nullable;
import java.util.UUID;

@Component
public class LicenseTypeService {
    public static final int MAX_INT_LICENSE_FIELD_SIZE = 999999999;

    private final LicenseTypeRepository licenseTypeRepository;
    private final OrganizationAccountLicenseRepository orgAccountLicenseRepository;

    @Autowired
    public LicenseTypeService(LicenseTypeRepository licenseTypeRepository, OrganizationAccountLicenseRepository orgAccountLicenseRepository) {
        this.licenseTypeRepository = licenseTypeRepository;
        this.orgAccountLicenseRepository = orgAccountLicenseRepository;
    }

    public PagedResponseModel getLicenseTypes(Integer pageOffset, Integer pageLimit, String query) {
        PageRequestValidationUtils.validatePagingParams(pageOffset, pageLimit);
        // TODO implement
        return null;
    }

    public LicenseTypeResponseModel getLicenseType(UUID licenseTypeId) {
        LicenseTypeEntity foundLicense = findLicenseType(licenseTypeId);
        return LicenseTypeResponseModel.fromEntity(foundLicense);
    }

    public LicenseTypeResponseModel createLicenseType(LicenseTypeCreationRequestModel requestModel) {
        validateRequestModel(requestModel);
        validateIntField("usersPerTeam", requestModel.getUsersPerTeam());
        boolean licenseTypeWithNameExists = licenseTypeRepository.existsByName(requestModel.getName());
        if (licenseTypeWithNameExists) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("The license type with the name '%s' already exists", requestModel.getName()));
        }

        // TODO implement
        return null;
    }

    public void updateLicenseType(UUID licenseTypeId, LicenseTypeUpdateRequestModel requestModel) {
        LicenseTypeEntity foundLicense = findLicenseType(licenseTypeId);
        validateRequestModel(requestModel);
        // TODO implement
    }

    public void deleteLicenseType(UUID licenseTypeId) {
        LicenseTypeEntity foundLicense = findLicenseType(licenseTypeId);
        boolean isLicenseInUse = orgAccountLicenseRepository.existsByLicenseTypeId(licenseTypeId);
        if (isLicenseInUse) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot delete a license that is in use by an organization account");
        }
        licenseTypeRepository.delete(foundLicense);
    }

    private LicenseTypeEntity findLicenseType(UUID licenseTypeId) {
        return licenseTypeRepository.findById(licenseTypeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    private void validateRequestModel(AbstractLicenseTypeRequestModel requestModel) {
        if (StringUtils.isBlank(requestModel.getName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The field 'name' must not be blank");
        }
        validateIntField("campaignsPerUserPerMonth", requestModel.getCampaignsPerUserPerMonth());
        validateIntField("contactsPerCampaign", requestModel.getContactsPerCampaign());
    }

    private void validateIntField(String fieldName, @Nullable Integer fieldValue) {
        if (null == fieldValue) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("The field '%s' is required", fieldName));
        } else if (fieldValue < 1 || fieldValue > MAX_INT_LICENSE_FIELD_SIZE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("The field '%s' must be greater than 0, and less than %d", fieldName, MAX_INT_LICENSE_FIELD_SIZE + 1));
        }
    }

}
