package com.usepipeline.portal.web.license;

import com.usepipeline.portal.common.FieldValidationUtils;
import com.usepipeline.portal.common.enumeration.LicenseType;
import com.usepipeline.portal.common.service.LicenseGenerator;
import com.usepipeline.portal.database.account.entity.LicenseEntity;
import com.usepipeline.portal.database.account.repository.LicenseRepository;
import com.usepipeline.portal.database.organization.OrganizationEntity;
import com.usepipeline.portal.database.organization.OrganizationRepository;
import com.usepipeline.portal.database.organization.account.OrganizationAccountEntity;
import com.usepipeline.portal.database.organization.account.OrganizationAccountRepository;
import com.usepipeline.portal.web.common.model.ActiveStatusPatchModel;
import com.usepipeline.portal.web.license.model.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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

    public LicenseModel getLicense(Long licenseId) {
        return licenseRepository.findById(licenseId)
                .map(this::convertToLicenseModel)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    // TODO this may need to be replaced with paging in the future
    public AllLicensesModel getAllLicenses() {
        List<LicenseModel> allLicenses = licenseRepository.findAll()
                .stream()
                .map(this::convertToLicenseModel)
                .collect(Collectors.toList());
        return new AllLicensesModel(allLicenses);
    }

    public LicenseModel createLicense(LicenseCreationRequestModel requestModel) {
        validateLicenseCreationRequestModel(requestModel);
        LicenseType licenseType = LicenseType.valueOf(requestModel.getType());
        LocalDate expirationDate = requestModel.getExpirationDate().toLocalDate();

        return licenseGenerator.generateLicense(licenseType, requestModel.getLicenseSeats(), requestModel.getMonthlyCost(), expirationDate);
    }

    public void updateLicense(Long licenseId, LicenseCreationRequestModel requestModel) {
        LicenseEntity existingLicense = licenseRepository.findById(licenseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        validateLicenseCreationRequestModel(requestModel);
        LocalDate expirationDate = requestModel.getExpirationDate().toLocalDate();

        existingLicense.setType(requestModel.getType());
        existingLicense.setLicenseSeats(requestModel.getLicenseSeats());
        existingLicense.setMonthlyCost(requestModel.getMonthlyCost());
        existingLicense.setExpirationDate(expirationDate);
        licenseRepository.save(existingLicense);
    }

    public void setActiveStatus(Long licenseId, ActiveStatusPatchModel activeStatusModel) {
        if (activeStatusModel.getActiveStatus() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The field activeStatus is required");
        }

        LicenseEntity licenseEntity = licenseRepository.findById(licenseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        licenseEntity.setIsActive(activeStatusModel.getActiveStatus());
        licenseRepository.save(licenseEntity);
    }

    private LicenseModel convertToLicenseModel(LicenseEntity licenseEntity) {
        if (licenseEntity.getIsActive()) {
            // TODO If the license is set to active and the account doesn't exist, what should we do?
            //  This is an admin endpoint, so rather than throwing a 500 error, we could display the error in the organization account name.
            Optional<OrganizationAccountEntity> optionalOrganizationAccount = organizationAccountRepository.findFirstByLicenseId(licenseEntity.getLicenseId());
            if (optionalOrganizationAccount.isPresent()) {
                OrganizationAccountEntity organizationAccount = optionalOrganizationAccount.get();
                String organizationName = organizationRepository.findById(organizationAccount.getOrganizationId())
                        .map(OrganizationEntity::getOrganizationName)
                        .orElse("ERROR: no Organization Name found in database");

                LicensedOrganizationAccountModel licensedAccount = new LicensedOrganizationAccountModel(organizationAccount.getOrganizationAccountId(), organizationName, organizationAccount.getOrganizationAccountName());
                return ActiveLicenseModel.fromLicenseEntity(licenseEntity, licensedAccount);
            }
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

        if (requestModel.getMonthlyCost() < 0.0) {
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
                .filter(type -> !type.equals(LicenseType.PIPELINE_TEAM))
                .map(LicenseType::name)
                .anyMatch(licenseType::equals);
    }

}
