package ai.salesfox.portal.rest.api.license.organization;

import ai.salesfox.portal.database.account.entity.MembershipEntity;
import ai.salesfox.portal.database.account.entity.UserEntity;
import ai.salesfox.portal.database.license.LicenseTypeEntity;
import ai.salesfox.portal.database.license.LicenseTypeRepository;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Component
public class OrganizationAccountLicenseService {
    private static final String INVALID_LICENSE_TYPE_MESSAGE = "The requested license type is invalid";

    private final OrganizationAccountRepository organizationAccountRepository;
    private final OrganizationAccountLicenseRepository orgAcctLicenseRepository;
    private final LicenseTypeRepository licenseTypeRepository;
    private final HttpSafeUserMembershipRetrievalService membershipRetrievalService;

    @Autowired
    public OrganizationAccountLicenseService(OrganizationAccountRepository organizationAccountRepository, OrganizationAccountLicenseRepository orgAcctLicenseRepository, LicenseTypeRepository licenseTypeRepository,
                                             HttpSafeUserMembershipRetrievalService membershipRetrievalService) {
        this.organizationAccountRepository = organizationAccountRepository;
        this.orgAcctLicenseRepository = orgAcctLicenseRepository;
        this.licenseTypeRepository = licenseTypeRepository;
        this.membershipRetrievalService = membershipRetrievalService;
    }

    public OrganizationAccountLicenseResponseModel getLicense(UUID orgAcctId) {
        OrganizationAccountEntity foundOrgAcct = findOrgAcct(orgAcctId);
        validateOrgAcctAccess(foundOrgAcct);
        OrganizationAccountLicenseEntity orgAcctLicense = findOrgAcctLicense(foundOrgAcct);
        return OrganizationAccountLicenseResponseModel.fromEntity(orgAcctLicense);
    }

    @Transactional
    public void updateLicense(UUID orgAcctId, OrganizationAccountLicenseTypeUpdateRequestModel requestModel) {
        OrganizationAccountEntity foundOrgAcct = findOrgAcct(orgAcctId);
        validateOrgAcctAccess(foundOrgAcct);

        LicenseTypeEntity foundLicenseType = validateRequestModelAndFindLicenseType(requestModel);
        if (!membershipRetrievalService.isAuthenticatedUserPortalAdmin() && !foundLicenseType.getIsPublic()) {
            // Only admins can assign non-public license types
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, INVALID_LICENSE_TYPE_MESSAGE);
        }

        OrganizationAccountLicenseEntity orgAcctLicense = findOrgAcctLicense(foundOrgAcct);
        orgAcctLicense.setLicenseTypeId(requestModel.getLicenseTypeId());
        orgAcctLicenseRepository.save(orgAcctLicense);
    }

    private OrganizationAccountEntity findOrgAcct(UUID orgAcctId) {
        return organizationAccountRepository.findById(orgAcctId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    private LicenseTypeEntity validateRequestModelAndFindLicenseType(OrganizationAccountLicenseTypeUpdateRequestModel requestModel) {
        UUID licenseTypeId = requestModel.getLicenseTypeId();
        if (null == licenseTypeId) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The field 'licenseTypeId' is required");
        }
        return licenseTypeRepository.findById(licenseTypeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, INVALID_LICENSE_TYPE_MESSAGE));
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
