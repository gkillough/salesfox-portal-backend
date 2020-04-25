package com.usepipeline.portal.web.organization.activation;

import com.usepipeline.portal.database.account.entity.MembershipEntity;
import com.usepipeline.portal.database.account.entity.UserEntity;
import com.usepipeline.portal.database.account.repository.MembershipRepository;
import com.usepipeline.portal.database.account.repository.UserRepository;
import com.usepipeline.portal.database.organization.account.OrganizationAccountEntity;
import com.usepipeline.portal.database.organization.account.OrganizationAccountRepository;
import com.usepipeline.portal.web.common.model.ActiveStatusPatchModel;
import com.usepipeline.portal.web.license.LicenseService;
import com.usepipeline.portal.web.user.active.UserActiveService;
import com.usepipeline.portal.web.util.HttpSafeUserMembershipRetrievalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import javax.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class OrganizationActivationService {
    private HttpSafeUserMembershipRetrievalService membershipRetrievalService;
    private MembershipRepository membershipRepository;
    private UserRepository userRepository;
    private OrganizationAccountRepository organizationAccountRepository;
    private LicenseService licenseService;
    private UserActiveService userActiveService;

    @Autowired
    public OrganizationActivationService(HttpSafeUserMembershipRetrievalService membershipRetrievalService, MembershipRepository membershipRepository, UserRepository userRepository,
                                         OrganizationAccountRepository organizationAccountRepository, LicenseService licenseService, UserActiveService userActiveService) {
        this.membershipRetrievalService = membershipRetrievalService;
        this.membershipRepository = membershipRepository;
        this.userRepository = userRepository;
        this.organizationAccountRepository = organizationAccountRepository;
        this.licenseService = licenseService;
        this.userActiveService = userActiveService;
    }

    @Transactional
    public void updateOrganizationAccountActiveStatus(Long organizationAccountId, ActiveStatusPatchModel activeStatusModel) {
        validateUpdatePermission(organizationAccountId);
        if (activeStatusModel.getActiveStatus() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The field 'activeStatus' cannot be null");
        }

        OrganizationAccountEntity orgAccountEntity = organizationAccountRepository.findById(organizationAccountId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        licenseService.setActiveStatus(orgAccountEntity.getLicenseId(), activeStatusModel);

        setActiveStatusForOrgUsers(organizationAccountId, activeStatusModel.getActiveStatus());

        orgAccountEntity.setIsActive(activeStatusModel.getActiveStatus());
        organizationAccountRepository.save(orgAccountEntity);
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
        List<Long> orgMemberUserIds = membershipRepository.findByOrganizationAccountId(orgAccountId)
                .stream()
                .map(MembershipEntity::getUserId)
                .collect(Collectors.toList());

        List<UserEntity> users = userRepository.findAllById(orgMemberUserIds);
        for (UserEntity user : users) {
            // TODO figure out how to avoid reactivating "permanently" deactivated users
            userActiveService.updateUserActiveStatus(user.getUserId(), activeStatus);
        }
    }

}
