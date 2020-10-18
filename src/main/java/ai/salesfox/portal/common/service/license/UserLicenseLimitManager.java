package ai.salesfox.portal.common.service.license;

import ai.salesfox.portal.common.time.PortalDateTimeUtils;
import ai.salesfox.portal.database.account.entity.MembershipEntity;
import ai.salesfox.portal.database.account.entity.UserEntity;
import ai.salesfox.portal.database.campaign.UserCampaignSendDateEntity;
import ai.salesfox.portal.database.campaign.UserCampaignSendDateRepository;
import ai.salesfox.portal.database.license.LicenseTypeEntity;
import ai.salesfox.portal.database.license.OrganizationAccountLicenseEntity;
import ai.salesfox.portal.database.organization.account.OrganizationAccountEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class UserLicenseLimitManager {
    private final UserCampaignSendDateRepository userCampaignSendDateRepository;

    @Autowired
    public UserLicenseLimitManager(UserCampaignSendDateRepository userCampaignSendDateRepository) {
        this.userCampaignSendDateRepository = userCampaignSendDateRepository;
    }

    public boolean isCampaignLimitReachedForUser(UserEntity user) {
        OrganizationAccountLicenseEntity orgAcctLicense = retrieveOrgAccountLicenseForUser(user);
        LicenseTypeEntity licenseType = orgAcctLicense.getLicenseTypeEntity();

        int campaignLimit = licenseType.getCampaignsPerUserPerMonth();

        LocalDate billingPeriodStartDate = PortalDateTimeUtils.computeMostRecentDateWithDayOfMonth(orgAcctLicense.getBillingDayOfMonth());
        List<UserCampaignSendDateEntity> userCampaignsForBillingPeriod = userCampaignSendDateRepository.findByUserIdAfter(user.getUserId(), billingPeriodStartDate);

        return userCampaignsForBillingPeriod.size() >= campaignLimit;
    }

    public void trackCampaignSentByUser(UserEntity user, int recipientCount) {
        LocalDate campaignDate = PortalDateTimeUtils.getCurrentDate();
        UserCampaignSendDateEntity campaignTrackingEntry = new UserCampaignSendDateEntity(null, user.getUserId(), campaignDate, recipientCount);
        userCampaignSendDateRepository.save(campaignTrackingEntry);
    }

    public int retrieveContactPerCampaignLimit(UserEntity user) {
        LicenseTypeEntity licenseType = retrieveLicenseTypeForUser(user);
        return licenseType.getContactsPerCampaign();
    }

    public OrganizationAccountLicenseEntity retrieveOrgAccountLicenseForUser(UserEntity user) {
        MembershipEntity userMembership = user.getMembershipEntity();
        OrganizationAccountEntity orgAccount = userMembership.getOrganizationAccountEntity();
        return orgAccount.getOrganizationAccountLicenseEntity();
    }

    public LicenseTypeEntity retrieveLicenseTypeForUser(UserEntity user) {
        MembershipEntity userMembership = user.getMembershipEntity();
        OrganizationAccountEntity orgAccount = userMembership.getOrganizationAccountEntity();
        OrganizationAccountLicenseEntity orgAccountLicense = orgAccount.getOrganizationAccountLicenseEntity();
        return orgAccountLicense.getLicenseTypeEntity();
    }

}
