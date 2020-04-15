package com.usepipeline.portal.web.license;

import com.usepipeline.portal.common.FieldValidationUtils;
import com.usepipeline.portal.common.enumeration.LicenseType;
import com.usepipeline.portal.common.model.PortalDateModel;
import com.usepipeline.portal.common.service.LicenseGenerator;
import com.usepipeline.portal.database.account.repository.LicenseRepository;
import com.usepipeline.portal.web.license.model.LicenseCreationRequestModel;
import com.usepipeline.portal.web.license.model.LicenseModel;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
public class LicenseService {
    private LicenseRepository licenseRepository;
    private LicenseGenerator licenseGenerator;

    @Autowired
    public LicenseService(LicenseRepository licenseRepository, LicenseGenerator licenseGenerator) {
        this.licenseRepository = licenseRepository;
        this.licenseGenerator = licenseGenerator;
    }

    public LicenseModel createLicense(LicenseCreationRequestModel requestModel) {
        validateLicenseCreationRequestModel(requestModel);
        LicenseType licenseType = LicenseType.valueOf(requestModel.getType());
        PortalDateModel expirationDateModel = requestModel.getExpirationDate();
        LocalDate expirationDate = LocalDate.of(expirationDateModel.getYear(), expirationDateModel.getMonth(), expirationDateModel.getDay());

        return licenseGenerator.generateLicense(licenseType, requestModel.getLicenseSeats(), requestModel.getMonthlyCost(), expirationDate);
    }

    private void validateLicenseCreationRequestModel(LicenseCreationRequestModel requestModel) {
        List<String> errorFields = new ArrayList<>();
        if (StringUtils.isBlank(requestModel.getType())) {
            errorFields.add("License Type is blank");
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

        if (LocalDate.now().isBefore(requestModel.getExpirationDate().toLocalDate())) {
            errorFields.add("Expiration Date must be in the future");
        }

        if (!errorFields.isEmpty()) {
            String errorFieldsString = String.join(", ", errorFields);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("There are errors with the fields: %s", errorFieldsString));
        }
    }

}
