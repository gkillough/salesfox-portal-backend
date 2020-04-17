package com.usepipeline.portal.common.service;

import com.usepipeline.portal.common.enumeration.LicenseType;
import com.usepipeline.portal.database.account.entity.LicenseEntity;
import com.usepipeline.portal.database.account.repository.LicenseRepository;
import com.usepipeline.portal.web.license.model.LicenseModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.UUID;

@Component
public class LicenseGenerator {
    private LicenseRepository licenseRepository;

    @Autowired
    public LicenseGenerator(LicenseRepository licenseRepository) {
        this.licenseRepository = licenseRepository;
    }

    /**
     * @return a LicenseModel corresponding to an inactive license entry in the database
     */
    public LicenseModel generateLicense(LicenseType type, long licenseSeats, Double monthlyCost, LocalDate expirationDate) {
        UUID licenseHash = UUID.randomUUID();
        LicenseEntity licenseEntity = new LicenseEntity(null, licenseHash, expirationDate, type.name(), licenseSeats, monthlyCost, false);
        LicenseEntity savedLicense = licenseRepository.save(licenseEntity);
        return LicenseModel.fromEntity(savedLicense);
    }

}
