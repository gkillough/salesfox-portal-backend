package ai.salesfox.portal.common.service.license;

import ai.salesfox.portal.common.enumeration.LicenseType;
import ai.salesfox.portal.database.account.entity.LicenseEntity;
import ai.salesfox.portal.database.account.repository.LicenseRepository;
import ai.salesfox.portal.rest.api.license.model.LicenseModel;
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
