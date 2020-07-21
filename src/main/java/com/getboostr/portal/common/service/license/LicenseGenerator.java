package com.getboostr.portal.common.service.license;

import com.getboostr.portal.common.enumeration.LicenseType;
import com.getboostr.portal.database.account.entity.LicenseEntity;
import com.getboostr.portal.database.account.repository.LicenseRepository;
import com.getboostr.portal.rest.license.model.LicenseModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
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
    public LicenseModel generateLicense(LicenseType type, long licenseSeats, BigDecimal monthlyCost, LocalDate expirationDate) {
        UUID licenseHash = UUID.randomUUID();

        // Initially, the availableLicenseSeats and maxLicenseSeats are the same because no account or users will exist for the license.
        LicenseEntity licenseEntity = new LicenseEntity(null, licenseHash, expirationDate, type.name(), licenseSeats, licenseSeats, monthlyCost, false);
        LicenseEntity savedLicense = licenseRepository.save(licenseEntity);
        return LicenseModel.fromEntity(savedLicense);
    }

}
