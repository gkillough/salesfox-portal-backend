package ai.salesfox.portal.common.service.license;

import ai.salesfox.portal.common.exception.PortalDatabaseIntegrityViolationException;
import ai.salesfox.portal.database.account.entity.LicenseEntity;
import ai.salesfox.portal.database.account.repository.LicenseRepository;
import ai.salesfox.portal.database.organization.account.OrganizationAccountEntity;
import ai.salesfox.portal.database.organization.account.OrganizationAccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class LicenseSeatManager {
    private final LicenseRepository licenseRepository;
    private final OrganizationAccountRepository organizationAccountRepository;

    @Autowired
    public LicenseSeatManager(LicenseRepository licenseRepository, OrganizationAccountRepository organizationAccountRepository) {
        this.licenseRepository = licenseRepository;
        this.organizationAccountRepository = organizationAccountRepository;
    }

    public LicenseEntity getLicenseForOrganizationAccount(OrganizationAccountEntity organizationAccountEntity) throws PortalDatabaseIntegrityViolationException {
        return getLicenseForOrganizationAccountId(organizationAccountEntity.getOrganizationAccountId());
    }

    public LicenseEntity getLicenseForOrganizationAccountId(UUID orgAccountId) throws PortalDatabaseIntegrityViolationException {
        return organizationAccountRepository.findById(orgAccountId)
                .flatMap(orgAcct -> licenseRepository.findById(orgAcct.getLicenseId()))
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
