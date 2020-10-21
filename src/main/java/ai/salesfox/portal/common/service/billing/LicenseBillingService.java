package ai.salesfox.portal.common.service.billing;

import ai.salesfox.portal.database.organization.account.OrganizationAccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Component
public class LicenseBillingService {
    private final OrganizationAccountRepository organizationAccountRepository;

    @Autowired
    public LicenseBillingService(OrganizationAccountRepository organizationAccountRepository) {
        this.organizationAccountRepository = organizationAccountRepository;
    }

    public void updateLicenseMonthlyCost(UUID licenseTypeId, BigDecimal newMonthlyCost) {
        // TODO implement
    }

    public void updateLicenseUsersIncluded(UUID licenseTypeId, Integer newUsersIncluded, Integer previousUsersIncluded, BigDecimal newCostPerAdditionalUser) {
        // TODO implement
    }

}
