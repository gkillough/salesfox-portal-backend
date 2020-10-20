package ai.salesfox.portal.common.service.license;

import ai.salesfox.portal.common.time.PortalDateTimeUtils;
import ai.salesfox.portal.database.account.entity.MembershipEntity;
import ai.salesfox.portal.database.account.entity.UserEntity;
import ai.salesfox.portal.database.campaign.UserCampaignSummaryEntity;
import ai.salesfox.portal.database.campaign.UserCampaignSummaryRepository;
import ai.salesfox.portal.database.license.LicenseTypeEntity;
import ai.salesfox.portal.database.license.OrganizationAccountLicenseEntity;
import ai.salesfox.portal.database.organization.account.OrganizationAccountEntity;
import ai.salesfox.portal.rest.api.license.type.LicenseTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class UserLicenseLimitManager {
    private final UserCampaignSummaryRepository userCampaignSummaryRepository;

    @Autowired
    public UserLicenseLimitManager(UserCampaignSummaryRepository userCampaignSummaryRepository) {
        this.userCampaignSummaryRepository = userCampaignSummaryRepository;
    }

    public boolean isCampaignLimitReachedForUser(UserEntity user) {
        OrganizationAccountLicenseEntity orgAcctLicense = retrieveOrgAccountLicenseForUser(user);
        LicenseTypeEntity licenseType = orgAcctLicense.getLicenseTypeEntity();

        int campaignLimit = licenseType.getCampaignsPerUserPerMonth();

        LocalDate billingPeriodStartDate = PortalDateTimeUtils.computeMostRecentDateWithDayOfMonth(orgAcctLicense.getBillingDayOfMonth());
        PageRequest maximumSizePageRequest = PageRequest.of(0, LicenseTypeService.MAX_INT_LICENSE_FIELD_SIZE);
        Page<UserCampaignSummaryEntity> userCampaignsForBillingPeriod = userCampaignSummaryRepository.findByUserIdOnOrAfter(user.getUserId(), billingPeriodStartDate, maximumSizePageRequest);

        return userCampaignsForBillingPeriod.getNumberOfElements() >= campaignLimit;
    }

    public void trackCampaignSentByUser(UserEntity user, int recipientCount) {
        LocalDate campaignDate = PortalDateTimeUtils.getCurrentDate();
        UserCampaignSummaryEntity campaignTrackingEntry = new UserCampaignSummaryEntity(null, user.getUserId(), campaignDate, recipientCount);
        userCampaignSummaryRepository.save(campaignTrackingEntry);
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
