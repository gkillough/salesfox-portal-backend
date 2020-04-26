package com.usepipeline.portal.common.service.license;

import com.usepipeline.portal.common.exception.PortalDatabaseIntegrityViolationException;
import com.usepipeline.portal.database.account.entity.LicenseEntity;
import com.usepipeline.portal.database.account.repository.LicenseRepository;
import com.usepipeline.portal.database.organization.account.OrganizationAccountEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class LicenseSeatManager {
    private LicenseRepository licenseRepository;

    @Autowired
    public LicenseSeatManager(LicenseRepository licenseRepository) {
        this.licenseRepository = licenseRepository;
    }

    public LicenseEntity getLicenseForOrganizationAccount(OrganizationAccountEntity organizationAccountEntity) throws PortalDatabaseIntegrityViolationException {
        return getLicenseForOrganizationAccountId(organizationAccountEntity.getOrganizationAccountId());
    }

    public LicenseEntity getLicenseForOrganizationAccountId(Long orgAccountId) throws PortalDatabaseIntegrityViolationException {
        return licenseRepository.findById(orgAccountId)
                .orElseThrow(() -> new PortalDatabaseIntegrityViolationException(String.format("Missing license for Organization Account Entity with id: [%d]", orgAccountId)));
    }

    public boolean hasAvailableSeats(LicenseEntity licenseEntity) {
        return licenseEntity.getAvailableLicenseSeats() > 0;
    }

    /**
     * @return the updated LicenseEntity
     */
    public LicenseEntity fillSeat(LicenseEntity licenseEntity) throws PortalLicenseSeatException {
        if (hasAvailableSeats(licenseEntity)) {
            Long availableLicenseSeats = licenseEntity.getAvailableLicenseSeats();
            licenseEntity.setAvailableLicenseSeats(availableLicenseSeats - 1);
            return licenseRepository.save(licenseEntity);
        }
        throw new PortalLicenseSeatException("No available license seats");
    }

    /**
     * @return the updated LicenseEntity
     */
    public LicenseEntity vacateSeat(LicenseEntity licenseEntity) throws PortalLicenseSeatException {
        if (licenseEntity.getAvailableLicenseSeats() != licenseEntity.getMaxLicenseSeats()) {
            Long availableLicenseSeats = licenseEntity.getAvailableLicenseSeats();
            licenseEntity.setAvailableLicenseSeats(availableLicenseSeats + 1);
            return licenseRepository.save(licenseEntity);
        }
        throw new PortalLicenseSeatException("No license seats occupied");
    }

}
