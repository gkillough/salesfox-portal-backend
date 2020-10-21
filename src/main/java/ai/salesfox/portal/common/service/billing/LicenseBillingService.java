package ai.salesfox.portal.common.service.billing;

import ai.salesfox.portal.database.license.OrganizationAccountLicenseEntity;
import ai.salesfox.portal.database.license.OrganizationAccountLicenseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.util.Streamable;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

@Component
public class LicenseBillingService {
    public static final PageRequest DEFAULT_ORG_ACCT_LICENSE_PAGE_REQUEST = PageRequest.of(0, 100);

    private final OrganizationAccountLicenseRepository organizationAccountRepository;

    @Autowired
    public LicenseBillingService(OrganizationAccountLicenseRepository organizationAccountRepository) {
        this.organizationAccountRepository = organizationAccountRepository;
    }

    public void updateLicenseMonthlyCost(UUID licenseTypeId, BigDecimal newMonthlyCost) {
        doLicenseUpdate(
                pageable -> organizationAccountRepository.findByLicenseTypeId(licenseTypeId, pageable),
                batchOfOrgAcctLicenses -> updateLicenseMonthlyCost(batchOfOrgAcctLicenses, newMonthlyCost)
        );
    }

    public void updateLicenseUsersIncluded(UUID licenseTypeId, Integer newUsersIncluded, Integer previousUsersIncluded, BigDecimal newCostPerAdditionalUser, BigDecimal previousCostPerAdditionalUser) {
        Function<Pageable, Slice<OrganizationAccountLicenseEntity>> getPage = null;
        if (newUsersIncluded < previousUsersIncluded) {
            // The number of users included is decreasing, so only get org acct licenses with more users than the new value.
            getPage = pageable -> organizationAccountRepository.findByLicenseTypeIdAndActiveUsersGreaterThan(licenseTypeId, newUsersIncluded, pageable);
        } else if (newUsersIncluded > previousUsersIncluded) {
            // The number of users included is increasing, so get org acct licenses with more users than the old value.
            getPage = pageable -> organizationAccountRepository.findByLicenseTypeIdAndActiveUsersGreaterThan(licenseTypeId, previousUsersIncluded, pageable);
        } else if (!newCostPerAdditionalUser.equals(previousCostPerAdditionalUser)) {
            // TODO we might not need to update org acct licenses individually and instead, update a single "item" price in Stripe
            // The number of users included didn't change, but the price per additional user did, so all org accounts for that license type need to be updated.
            getPage = pageable -> organizationAccountRepository.findByLicenseTypeId(licenseTypeId, pageable);
        }

        if (null != getPage) {
            doLicenseUpdate(
                    getPage,
                    (batchOfOrgAcctLicenses) -> updateBillingForAdditionalUsers(batchOfOrgAcctLicenses, newUsersIncluded, previousUsersIncluded, newCostPerAdditionalUser, previousCostPerAdditionalUser)
            );
        }
    }

    // TODO we might not need to update org acct licenses individually and instead, update a single subscription "item" price in Stripe
    private void updateLicenseMonthlyCost(Streamable<OrganizationAccountLicenseEntity> batchOfOrgAcctLicenses, BigDecimal newMonthlyCost) {
        // TODO implement
    }

    private void updateBillingForAdditionalUsers(
            Streamable<OrganizationAccountLicenseEntity> batchOfOrgAcctLicenses,
            Integer newUsersIncluded,
            Integer previousUsersIncluded,
            BigDecimal newCostPerAdditionalUser,
            BigDecimal previousCostPerAdditionalUser
    ) {
        // TODO implement
    }

    private void doLicenseUpdate(Function<Pageable, Slice<OrganizationAccountLicenseEntity>> getPage, Consumer<Streamable<OrganizationAccountLicenseEntity>> updateBatchOfOrgAcctLicenses) {
        Pageable pageRequest = DEFAULT_ORG_ACCT_LICENSE_PAGE_REQUEST;

        Slice<OrganizationAccountLicenseEntity> pageOfOrgAccountLicenses;
        do {
            pageOfOrgAccountLicenses = getPage.apply(pageRequest);
            updateBatchOfOrgAcctLicenses.accept(pageOfOrgAccountLicenses);
            pageRequest = pageOfOrgAccountLicenses.nextPageable();
        } while (pageOfOrgAccountLicenses.hasNext());
    }

}
