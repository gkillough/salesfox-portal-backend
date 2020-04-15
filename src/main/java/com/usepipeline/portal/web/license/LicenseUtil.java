package com.usepipeline.portal.web.license;

import com.usepipeline.portal.common.enumeration.LicenseType;
import com.usepipeline.portal.database.account.entity.LicenseEntity;
import com.usepipeline.portal.database.account.repository.LicenseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.UUID;

@Component
public class LicenseUtil {
    private LicenseRepository licenseRepository;

    @Autowired
    public LicenseUtil(LicenseRepository licenseRepository) {
        this.licenseRepository = licenseRepository;
    }

    /**
     * @return a LicenseModel corresponding to an inactive license entry in the database
     */
    public LicenseModel createLicense(LicenseType type, int licenseSeats, Double monthlyCost, LocalDate expirationDate) {
        UUID licenseHash = UUID.randomUUID();
        LicenseEntity licenseEntity = new LicenseEntity(null, licenseHash, expirationDate, type.name(), licenseSeats, monthlyCost, false);
        LicenseEntity savedLicense = licenseRepository.save(licenseEntity);
        return LicenseModel.fromEntity(savedLicense);
    }

}
