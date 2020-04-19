package com.usepipeline.portal.web.organization.activation;

import com.usepipeline.portal.database.account.entity.MembershipEntity;
import com.usepipeline.portal.database.account.entity.UserEntity;
import com.usepipeline.portal.database.account.repository.MembershipRepository;
import com.usepipeline.portal.database.organization.account.OrganizationAccountEntity;
import com.usepipeline.portal.database.organization.account.OrganizationAccountRepository;
import com.usepipeline.portal.web.common.model.ActiveStatusPatchModel;
import com.usepipeline.portal.web.license.LicenseService;
import com.usepipeline.portal.web.util.HttpSafeUserMembershipRetrievalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import javax.transaction.Transactional;
import java.util.List;

@Component
public class OrganizationActivationService {
    private HttpSafeUserMembershipRetrievalService membershipRetrievalService;
    private MembershipRepository membershipRepository;
    private OrganizationAccountRepository organizationAccountRepository;
    private LicenseService licenseService;

    @Autowired
    public OrganizationActivationService(HttpSafeUserMembershipRetrievalService membershipRetrievalService, MembershipRepository membershipRepository,
                                         OrganizationAccountRepository organizationAccountRepository, LicenseService licenseService) {
        this.membershipRetrievalService = membershipRetrievalService;
        this.membershipRepository = membershipRepository;
        this.organizationAccountRepository = organizationAccountRepository;
        this.licenseService = licenseService;
    }

    @Transactional
    public void updateOrganizationAccountActiveStatus(Long organizationAccountId, ActiveStatusPatchModel activeStatusModel) {
        validateUpdatePermission(organizationAccountId);
        if (activeStatusModel.getActiveStatus() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The field 'activeStatus' cannot be null");
        }

        setActiveStatusForOrgUsers(organizationAccountId, activeStatusModel.getActiveStatus());

        OrganizationAccountEntity orgAccountEntity = organizationAccountRepository.findById(organizationAccountId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        orgAccountEntity.setIsActive(activeStatusModel.getActiveStatus());
        organizationAccountRepository.save(orgAccountEntity);

        licenseService.setActiveStatus(orgAccountEntity.getLicenseId(), activeStatusModel);
    }

    private void validateUpdatePermission(Long organizationAccountId) {
        if (!membershipRetrievalService.isPipelineAdmin()) {
            UserEntity authenticatedUserEntity = membershipRetrievalService.getAuthenticatedUserEntity();
            MembershipEntity membershipEntity = membershipRetrievalService.getMembershipEntity(authenticatedUserEntity);
            if (membershipEntity.getOrganizationAccountId() != organizationAccountId) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN);
            }
        }
    }

    private void setActiveStatusForOrgUsers(Long orgAccountId, boolean activeStatus) {
        List<MembershipEntity> memberships = membershipRepository.findByOrganizationAccountId(orgAccountId);
        for (MembershipEntity membership : memberships) {
            membership.setIsActive(activeStatus);
            // TODO can we remove the isActive flag from UserEntity? If not, we also have to retrieve the user associated with the membership.
        }
        membershipRepository.saveAll(memberships);
    }

}
