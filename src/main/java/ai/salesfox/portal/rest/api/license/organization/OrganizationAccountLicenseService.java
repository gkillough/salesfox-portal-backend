package ai.salesfox.portal.rest.api.license.organization;

import ai.salesfox.portal.database.account.entity.MembershipEntity;
import ai.salesfox.portal.database.account.entity.UserEntity;
import ai.salesfox.portal.database.license.OrganizationAccountLicenseEntity;
import ai.salesfox.portal.database.license.OrganizationAccountLicenseRepository;
import ai.salesfox.portal.database.organization.account.OrganizationAccountEntity;
import ai.salesfox.portal.database.organization.account.OrganizationAccountRepository;
import ai.salesfox.portal.rest.api.license.organization.model.OrganizationAccountLicenseResponseModel;
import ai.salesfox.portal.rest.api.license.organization.model.OrganizationAccountLicenseTypeUpdateRequestModel;
import ai.salesfox.portal.rest.util.HttpSafeUserMembershipRetrievalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Component
public class OrganizationAccountLicenseService {
    private final OrganizationAccountRepository organizationAccountRepository;
    private final OrganizationAccountLicenseRepository orgAcctLicenseRepository;
    private final HttpSafeUserMembershipRetrievalService membershipRetrievalService;

    @Autowired
    public OrganizationAccountLicenseService(OrganizationAccountRepository organizationAccountRepository, OrganizationAccountLicenseRepository orgAcctLicenseRepository, HttpSafeUserMembershipRetrievalService membershipRetrievalService) {
        this.organizationAccountRepository = organizationAccountRepository;
        this.orgAcctLicenseRepository = orgAcctLicenseRepository;
        this.membershipRetrievalService = membershipRetrievalService;
    }

    public OrganizationAccountLicenseResponseModel getLicense(UUID orgAcctId) {
        OrganizationAccountEntity foundOrgAcct = findOrgAcctAndValidateAccess(orgAcctId);
        OrganizationAccountLicenseEntity orgAcctLicense = findOrgAcctLicense(foundOrgAcct);
        return OrganizationAccountLicenseResponseModel.fromEntity(orgAcctLicense);
    }

    public void updateLicense(UUID orgAcctId, OrganizationAccountLicenseTypeUpdateRequestModel requestModel) {
        OrganizationAccountEntity foundOrgAcct = findOrgAcctAndValidateAccess(orgAcctId);
        // FIXME implement
    }

    private OrganizationAccountEntity findOrgAcctAndValidateAccess(UUID orgAcctId) {
        OrganizationAccountEntity foundOrgAcct = organizationAccountRepository.findById(orgAcctId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        validateOrgAcctAccess(foundOrgAcct);
        return foundOrgAcct;
    }

    private OrganizationAccountLicenseEntity findOrgAcctLicense(OrganizationAccountEntity orgAcct) {
        return orgAcctLicenseRepository.findById(orgAcct.getOrganizationAccountId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "No license type is associated with this organization account"));
    }

    private void validateOrgAcctAccess(OrganizationAccountEntity orgAcct) {
        if (!membershipRetrievalService.isAuthenticatedUserPortalAdmin()) {
            UserEntity loggedInUser = membershipRetrievalService.getAuthenticatedUserEntity();
            MembershipEntity userMembership = loggedInUser.getMembershipEntity();
            if (!orgAcct.getOrganizationAccountId().equals(userMembership.getOrganizationAccountId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have access to this organization account");
            }
        }
    }

}
