package com.getboostr.portal.web.license;

import com.getboostr.portal.common.FieldValidationUtils;
import com.getboostr.portal.common.enumeration.LicenseType;
import com.getboostr.portal.common.service.license.LicenseGenerator;
import com.getboostr.portal.database.account.entity.LicenseEntity;
import com.getboostr.portal.database.account.repository.LicenseRepository;
import com.getboostr.portal.database.organization.OrganizationEntity;
import com.getboostr.portal.database.organization.OrganizationRepository;
import com.getboostr.portal.database.organization.account.OrganizationAccountEntity;
import com.getboostr.portal.web.license.model.*;
import com.getboostr.portal.database.organization.account.OrganizationAccountRepository;
import com.getboostr.portal.web.common.model.request.ActiveStatusPatchModel;
import com.usepipeline.portal.web.license.model.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class LicenseService {
    private LicenseRepository licenseRepository;
    private LicenseGenerator licenseGenerator;
    private OrganizationRepository organizationRepository;
    private OrganizationAccountRepository organizationAccountRepository;

    @Autowired
    public LicenseService(LicenseRepository licenseRepository, LicenseGenerator licenseGenerator, OrganizationRepository organizationRepository, OrganizationAccountRepository organizationAccountRepository) {
        this.licenseRepository = licenseRepository;
        this.licenseGenerator = licenseGenerator;
        this.organizationRepository = organizationRepository;
        this.organizationAccountRepository = organizationAccountRepository;
    }

    public LicenseModel getLicense(UUID licenseId) {
        return licenseRepository.findById(licenseId)
                .map(this::convertToLicenseModel)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    // TODO this may need to be replaced with paging in the future
    public MultiLicenseModel getAllLicenses() {
        List<LicenseModel> allLicenses = licenseRepository.findAll()
                .stream()
                .map(this::convertToLicenseModel)
                .collect(Collectors.toList());
        return new MultiLicenseModel(allLicenses);
    }

    public LicenseModel createLicense(LicenseCreationRequestModel requestModel) {
        validateLicenseCreationRequestModel(requestModel);
        LicenseType licenseType = LicenseType.valueOf(requestModel.getType());
        LocalDate expirationDate = requestModel.getExpirationDate().toLocalDate();

        return licenseGenerator.generateLicense(licenseType, requestModel.getLicenseSeats(), requestModel.getMonthlyCost(), expirationDate);
    }

    @Transactional
    public void updateLicense(UUID licenseId, LicenseCreationRequestModel requestModel) {
        LicenseEntity existingLicense = licenseRepository.findById(licenseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        validateLicenseCreationRequestModel(requestModel);
        LocalDate expirationDate = requestModel.getExpirationDate().toLocalDate();

        existingLicense.setType(requestModel.getType());
        existingLicense.setMonthlyCost(requestModel.getMonthlyCost());
        existingLicense.setExpirationDate(expirationDate);

        LicenseEntity savedLicense = licenseRepository.save(existingLicense);
        setMaxLicenseSeats(savedLicense.getLicenseId(), new LicenseSeatUpdateModel(requestModel.getLicenseSeats()));
    }

    public void setActiveStatus(UUID licenseId, ActiveStatusPatchModel activeStatusModel) {
        LicenseEntity licenseEntity = licenseRepository.findById(licenseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (activeStatusModel.getActiveStatus() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The field 'activeStatus' is required");
        }

        if (activeStatusModel.getActiveStatus() && hasLicenseExpired(licenseEntity)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot activate an expired license");
        }

        licenseEntity.setIsActive(activeStatusModel.getActiveStatus());
        licenseRepository.save(licenseEntity);
    }

    public void setMaxLicenseSeats(UUID licenseId, LicenseSeatUpdateModel licenseSeatUpdateModel) {
        LicenseEntity licenseEntity = licenseRepository.findById(licenseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (licenseSeatUpdateModel.getMaxLicenseSeats() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The field 'maxLicenseSeats' is required");
        }

        if (licenseSeatUpdateModel.getMaxLicenseSeats() < 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The field 'maxLicenseSeats' must be a positive integer");
        }

        Long currentMaxLicenseSeats = licenseEntity.getMaxLicenseSeats();
        Long currentAvailableLicenseSeats = licenseEntity.getAvailableLicenseSeats();

        Long currentOccupiedLicenseSeats = currentMaxLicenseSeats - currentAvailableLicenseSeats;
        if (licenseSeatUpdateModel.getMaxLicenseSeats() < currentOccupiedLicenseSeats) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The field 'maxLicenseSeats' cannot be less than the number of license seats in use");
        }

        Long differenceInLicenseSeats = currentMaxLicenseSeats - licenseSeatUpdateModel.getMaxLicenseSeats();
        Long newAvailableLicenseSeats = Math.subtractExact(currentAvailableLicenseSeats, differenceInLicenseSeats);

        licenseEntity.setMaxLicenseSeats(licenseSeatUpdateModel.getMaxLicenseSeats());
        licenseEntity.setAvailableLicenseSeats(newAvailableLicenseSeats);
        licenseRepository.save(licenseEntity);
    }

    private LicenseModel convertToLicenseModel(LicenseEntity licenseEntity) {
        if (licenseEntity.getIsActive()) {
            OrganizationAccountEntity organizationAccount = organizationAccountRepository.findFirstByLicenseId(licenseEntity.getLicenseId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Organization account is missing from the database"));
            String organizationName = organizationRepository.findById(organizationAccount.getOrganizationId())
                    .map(OrganizationEntity::getOrganizationName)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Organization is missing from the database"));

            LicensedOrganizationAccountModel licensedAccount = new LicensedOrganizationAccountModel(organizationAccount.getOrganizationAccountId(), organizationName, organizationAccount.getOrganizationAccountName());
            return ActiveLicenseModel.fromLicenseEntity(licenseEntity, licensedAccount);
        }
        return LicenseModel.fromEntity(licenseEntity);
    }

    private void validateLicenseCreationRequestModel(LicenseCreationRequestModel requestModel) {
        List<String> errorFields = new ArrayList<>();
        if (StringUtils.isBlank(requestModel.getType())) {
            errorFields.add("License Type is blank");
        }

        if (!isValidLicenseType(requestModel.getType())) {
            errorFields.add("License Type is invalid");
        }

        if (requestModel.getLicenseSeats() == null) {
            errorFields.add("License Seats is required");
        }

        if (requestModel.getLicenseSeats() < 1) {
            errorFields.add("License Seats must be a positive integer");
        }

        if (requestModel.getMonthlyCost() == null) {
            errorFields.add("Monthly Cost is required");
        }

        if (requestModel.getMonthlyCost().compareTo(BigDecimal.ZERO) <= 0) {
            errorFields.add("Monthly Cost cannot be negative");
        }

        if (requestModel.getExpirationDate() == null) {
            errorFields.add("Expiration Date is required");
        }

        if (!FieldValidationUtils.isValidDate(requestModel.getExpirationDate())) {
            errorFields.add("Expiration Date is invalid");
        }

        if (!LocalDate.now().isBefore(requestModel.getExpirationDate().toLocalDate())) {
            errorFields.add("Expiration Date must be in the future");
        }

        if (!errorFields.isEmpty()) {
            String errorFieldsString = String.join(", ", errorFields);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("There are errors with the fields: %s", errorFieldsString));
        }
    }

    private boolean isValidLicenseType(String licenseType) {
        return Stream
                .of(LicenseType.values())
                .filter(type -> !type.equals(LicenseType.BOOSTR_TEAM))
                .map(LicenseType::name)
                .anyMatch(licenseType::equals);
    }

    private boolean hasLicenseExpired(LicenseEntity license) {
        return !LocalDate.now().isBefore(license.getExpirationDate());
    }

}
