package ai.salesfox.portal.rest.api.license.type;

import ai.salesfox.portal.database.license.LicenseTypeEntity;
import ai.salesfox.portal.database.license.LicenseTypeRepository;
import ai.salesfox.portal.database.license.OrganizationAccountLicenseRepository;
import ai.salesfox.portal.event.license.type.LicenseTypeChangedEventPublisher;
import ai.salesfox.portal.rest.api.common.page.PageRequestValidationUtils;
import ai.salesfox.portal.rest.api.license.type.model.AbstractLicenseTypeModel;
import ai.salesfox.portal.rest.api.license.type.model.LicenseTypeRequestModel;
import ai.salesfox.portal.rest.api.license.type.model.LicenseTypeResponseModel;
import ai.salesfox.portal.rest.api.license.type.model.MultiLicenseTypeModel;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class LicenseTypeService {
    public static final int MAX_INT_LICENSE_FIELD_SIZE = 999999999;

    private final LicenseTypeRepository licenseTypeRepository;
    private final OrganizationAccountLicenseRepository orgAccountLicenseRepository;
    private final LicenseTypeChangedEventPublisher licenseTypeChangedEventPublisher;

    @Autowired
    public LicenseTypeService(LicenseTypeRepository licenseTypeRepository, OrganizationAccountLicenseRepository orgAccountLicenseRepository, LicenseTypeChangedEventPublisher licenseTypeChangedEventPublisher) {
        this.licenseTypeRepository = licenseTypeRepository;
        this.orgAccountLicenseRepository = orgAccountLicenseRepository;
        this.licenseTypeChangedEventPublisher = licenseTypeChangedEventPublisher;
    }

    public MultiLicenseTypeModel getPublicLicenseTypes(Integer pageOffset, Integer pageLimit) {
        PageRequestValidationUtils.validatePagingParams(pageOffset, pageLimit);
        PageRequest pageRequest = PageRequest.of(pageOffset, pageLimit);

        Page<LicenseTypeEntity> publicLicenseTypes = licenseTypeRepository.findByIsPublic(true, pageRequest);
        return convertToMultiResponseModel(publicLicenseTypes);
    }

    public MultiLicenseTypeModel getAllLicenseTypes(Integer pageOffset, Integer pageLimit, String nameQuery) {
        PageRequestValidationUtils.validatePagingParams(pageOffset, pageLimit);
        PageRequest pageRequest = PageRequest.of(pageOffset, pageLimit);

        Page<LicenseTypeEntity> foundLicenseTypesPage;
        if (StringUtils.isNotBlank(nameQuery)) {
            foundLicenseTypesPage = licenseTypeRepository.findByNameContaining(nameQuery, pageRequest);
        } else {
            foundLicenseTypesPage = licenseTypeRepository.findAll(pageRequest);
        }
        return convertToMultiResponseModel(foundLicenseTypesPage);
    }

    public LicenseTypeResponseModel getLicenseType(UUID licenseTypeId) {
        LicenseTypeEntity foundLicense = findLicenseType(licenseTypeId);
        return LicenseTypeResponseModel.fromEntity(foundLicense);
    }

    @Transactional
    public LicenseTypeResponseModel createLicenseType(LicenseTypeRequestModel requestModel) {
        validateRequestModel(requestModel, null);

        LicenseTypeEntity licenseTypeToSave = new LicenseTypeEntity(
                null,
                requestModel.getName(),
                requestModel.getIsPublic(),
                requestModel.getMonthlyCost(),
                requestModel.getCampaignsPerUserPerMonth(),
                requestModel.getContactsPerCampaign(),
                requestModel.getUsersIncluded(),
                requestModel.getCostPerAdditionalUser()
        );
        LicenseTypeEntity savedLicenseType = licenseTypeRepository.save(licenseTypeToSave);
        return LicenseTypeResponseModel.fromEntity(savedLicenseType);
    }

    @Transactional
    public void updateLicenseType(UUID licenseTypeId, LicenseTypeRequestModel requestModel) {
        LicenseTypeEntity foundLicense = findLicenseType(licenseTypeId);
        validateRequestModel(requestModel, foundLicense);

        LicenseTypeEntity updatedLicenseType = new LicenseTypeEntity(
                foundLicense.getLicenseTypeId(),
                requestModel.getName(),
                requestModel.getIsPublic(),
                requestModel.getMonthlyCost(),
                requestModel.getCampaignsPerUserPerMonth(),
                requestModel.getContactsPerCampaign(),
                requestModel.getUsersIncluded(),
                requestModel.getCostPerAdditionalUser()
        );

        BigDecimal previousMonthlyCost = new BigDecimal(foundLicense.getMonthlyCost().toString());
        int previousUsersIncluded = foundLicense.getUsersIncluded();
        BigDecimal previousCostPerAdditionalUser = new BigDecimal(foundLicense.getCostPerAdditionalUser().toString());

        licenseTypeRepository.save(updatedLicenseType);
        licenseTypeChangedEventPublisher.fireLicenseTypeChangedEvent(foundLicense.getLicenseTypeId(), previousMonthlyCost, previousUsersIncluded, previousCostPerAdditionalUser);
    }

    @Transactional
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

    private void validateRequestModel(AbstractLicenseTypeModel requestModel, @Nullable LicenseTypeEntity existingEntity) {
        if (StringUtils.isBlank(requestModel.getName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The field 'name' cannot be blank");
        }

        if (null == requestModel.getIsPublic()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The field 'isPublic' is required");
        }

        if (null == existingEntity || !existingEntity.getName().equals(requestModel.getName())) {
            boolean licenseTypeWithNameExists = licenseTypeRepository.existsByName(requestModel.getName());
            if (licenseTypeWithNameExists) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("The license type with the name '%s' already exists", requestModel.getName()));
            }
        }

        validateBigDecimalField("monthlyCost", requestModel.getMonthlyCost());
        validateIntField("campaignsPerUserPerMonth", requestModel.getCampaignsPerUserPerMonth());
        validateIntField("contactsPerCampaign", requestModel.getContactsPerCampaign());
        validateIntField("usersIncluded", requestModel.getUsersIncluded());
        validateBigDecimalField("costPerAdditionalUser", requestModel.getCostPerAdditionalUser());
    }

    private void validateIntField(String fieldName, @Nullable Integer fieldValue) {
        if (null == fieldValue) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("The field '%s' is required", fieldName));
        } else if (fieldValue < 1 || fieldValue > MAX_INT_LICENSE_FIELD_SIZE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("The field '%s' must be greater than 0, and less than %d", fieldName, MAX_INT_LICENSE_FIELD_SIZE + 1));
        }
    }

    private void validateBigDecimalField(String fieldName, @Nullable BigDecimal fieldValue) {
        if (null == fieldValue) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("The field '%s' is required", fieldName));
        } else if (fieldValue.compareTo(BigDecimal.ZERO) < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("The field '%s' cannot be less than 0.00", fieldName));
        }
    }

    // TODO this can likely be abstracted further
    private MultiLicenseTypeModel convertToMultiResponseModel(Page<LicenseTypeEntity> foundLicenseTypesPage) {
        if (foundLicenseTypesPage.isEmpty()) {
            return MultiLicenseTypeModel.empty();
        }

        List<LicenseTypeResponseModel> responseModels = foundLicenseTypesPage
                .stream()
                .map(LicenseTypeResponseModel::fromEntity)
                .collect(Collectors.toList());
        return new MultiLicenseTypeModel(responseModels, foundLicenseTypesPage);
    }

}
