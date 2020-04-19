package com.usepipeline.portal.web.organization.profile;

import com.usepipeline.portal.common.enumeration.AccessLevel;
import com.usepipeline.portal.common.model.PortalAddressModel;
import com.usepipeline.portal.database.account.entity.UserEntity;
import com.usepipeline.portal.database.organization.OrganizationEntity;
import com.usepipeline.portal.database.organization.account.OrganizationAccountEntity;
import com.usepipeline.portal.database.organization.account.OrganizationAccountRepository;
import com.usepipeline.portal.database.organization.account.address.OrganizationAccountAddressEntity;
import com.usepipeline.portal.database.organization.account.address.OrganizationAccountAddressRepository;
import com.usepipeline.portal.database.organization.account.profile.OrganizationAccountProfileEntity;
import com.usepipeline.portal.database.organization.account.profile.OrganizationAccountProfileRepository;
import com.usepipeline.portal.web.organization.common.OrganizationAccessService;
import com.usepipeline.portal.web.organization.profile.model.OrganizationAccountProfileModel;
import com.usepipeline.portal.web.util.HttpSafeUserMembershipRetrievalService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Component
public class OrganizationProfileService {
    private HttpSafeUserMembershipRetrievalService membershipRetrievalService;
    private OrganizationAccessService organizationAccessService;
    private OrganizationAccountRepository organizationAccountRepository;
    private OrganizationAccountProfileRepository organizationAccountProfileRepository;
    private OrganizationAccountAddressRepository organizationAccountAddressRepository;

    @Autowired
    public OrganizationProfileService(HttpSafeUserMembershipRetrievalService membershipRetrievalService, OrganizationAccessService organizationAccessService,
                                      OrganizationAccountRepository organizationAccountRepository, OrganizationAccountProfileRepository organizationAccountProfileRepository, OrganizationAccountAddressRepository organizationAccountAddressRepository) {
        this.membershipRetrievalService = membershipRetrievalService;
        this.organizationAccessService = organizationAccessService;
        this.organizationAccountRepository = organizationAccountRepository;
        this.organizationAccountProfileRepository = organizationAccountProfileRepository;
        this.organizationAccountAddressRepository = organizationAccountAddressRepository;
    }

    public OrganizationAccountProfileModel getProfile(Long organizationAccountId) {
        OrganizationAccountEntity orgAccountEntity = organizationAccountRepository.findById(organizationAccountId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        UserEntity authenticatedUserEntity = membershipRetrievalService.getAuthenticatedUserEntity();
        AccessLevel accessLevel = organizationAccessService.getAccessLevelForUserRequestingAccount(authenticatedUserEntity, orgAccountEntity);
        if (AccessLevel.NONE.equals(accessLevel)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        OrganizationEntity organizationEntity = membershipRetrievalService.getOrganizationEntity(orgAccountEntity);
        OrganizationAccountProfileEntity orgAcctProfileEntity = getOrganizationAccountProfileEntity(organizationAccountId);
        OrganizationAccountAddressEntity orgAccountAddressEntity = getOrgAccountAddressEntity(orgAcctProfileEntity);
        PortalAddressModel orgAcctAddress = PortalAddressModel.fromEntity(orgAccountAddressEntity);

        return new OrganizationAccountProfileModel(organizationEntity.getOrganizationName(), orgAccountEntity.getOrganizationAccountName(), orgAcctProfileEntity.getBusinessNumber(), orgAcctAddress);
    }

    private OrganizationAccountProfileEntity getOrganizationAccountProfileEntity(Long orgAccountId) {
        return organizationAccountProfileRepository.findFirstByOrganizationAccountId(orgAccountId)
                .orElseThrow(() -> {
                    log.error("Missing organization account profile for organizationAccountId: [{}]", orgAccountId);
                    return new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
                });
    }

    private OrganizationAccountAddressEntity getOrgAccountAddressEntity(OrganizationAccountProfileEntity profileEntity) {
        return organizationAccountAddressRepository.findById(profileEntity.getMailingAddressId())
                .orElseThrow(() -> {
                    log.error("Missing organization account profile for organizationProfileId: [{}]", profileEntity.getProfileId());
                    return new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
                });
    }

}
