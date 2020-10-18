package ai.salesfox.portal.common.service.license;

import ai.salesfox.portal.common.time.PortalDateTimeUtils;
import ai.salesfox.portal.database.account.entity.MembershipEntity;
import ai.salesfox.portal.database.account.entity.UserEntity;
import ai.salesfox.portal.database.license.LicenseTypeEntity;
import ai.salesfox.portal.database.license.OrganizationAccountLicenseEntity;
import ai.salesfox.portal.database.organization.account.OrganizationAccountEntity;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class UserLicenseLimitManager {

    public boolean isCampaignLimitReachedForUser(UserEntity user) {
        LicenseTypeEntity licenseType = retrieveLicenseTypeForUser(user);
        int campaignLimit = licenseType.getCampaignsPerUserPerMonth();

        // FIXME implement
        return false;
    }

    public void trackCampaignSentByUser(UserEntity user) {
        LocalDate campaignDate = PortalDateTimeUtils.getCurrentDate();
        // FIXME implement
    }

    public List<Object> getCampaignsByUser() {
        // FIXME implement
        return List.of();
    }

    // Get contact-per-campaign limit
    public int retrieveContactPerCampaignLimit(UserEntity user) {
        LicenseTypeEntity licenseType = retrieveLicenseTypeForUser(user);
        return licenseType.getContactsPerCampaign();
    }

    public LicenseTypeEntity retrieveLicenseTypeForUser(UserEntity user) {
        MembershipEntity userMembership = user.getMembershipEntity();
        OrganizationAccountEntity orgAccount = userMembership.getOrganizationAccountEntity();
        OrganizationAccountLicenseEntity orgAccountLicense = orgAccount.getOrganizationAccountLicenseEntity();
        return orgAccountLicense.getLicenseTypeEntity();
    }

}
