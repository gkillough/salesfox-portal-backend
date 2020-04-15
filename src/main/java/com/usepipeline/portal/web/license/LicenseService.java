package com.usepipeline.portal.web.license;

import com.usepipeline.portal.common.enumeration.LicenseType;
import com.usepipeline.portal.common.model.PortalDateModel;
import com.usepipeline.portal.common.service.LicenseGenerator;
import com.usepipeline.portal.database.account.repository.LicenseRepository;
import com.usepipeline.portal.web.license.model.LicenseCreationRequestModel;
import com.usepipeline.portal.web.license.model.LicenseModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

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
        // TODO implement
    }

}
