package com.usepipeline.portal.web.organization.users;

import com.usepipeline.portal.common.enumeration.AccessLevel;
import com.usepipeline.portal.database.account.entity.MembershipEntity;
import com.usepipeline.portal.database.account.entity.RoleEntity;
import com.usepipeline.portal.database.account.entity.UserEntity;
import com.usepipeline.portal.database.account.repository.MembershipRepository;
import com.usepipeline.portal.database.account.repository.RoleRepository;
import com.usepipeline.portal.database.organization.account.OrganizationAccountEntity;
import com.usepipeline.portal.database.organization.account.OrganizationAccountRepository;
import com.usepipeline.portal.web.organization.common.OrganizationAccessService;
import com.usepipeline.portal.web.organization.users.model.NewAccountOwnerRequestModel;
import com.usepipeline.portal.web.security.authorization.PortalAuthorityConstants;
import com.usepipeline.portal.web.user.profile.UserProfileService;
import com.usepipeline.portal.web.user.profile.model.UserProfileModel;
import com.usepipeline.portal.web.util.HttpSafeUserMembershipRetrievalService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Slf4j
@Component
public class OrganizationUsersService {
    private HttpSafeUserMembershipRetrievalService membershipRetrievalService;
    private OrganizationAccessService organizationAccessService;
    private UserProfileService userProfileService;
    private OrganizationAccountRepository organizationAccountRepository;
    private RoleRepository roleRepository;
    private MembershipRepository membershipRepository;

    @Autowired
    public OrganizationUsersService(HttpSafeUserMembershipRetrievalService membershipRetrievalService, OrganizationAccessService organizationAccessService,
                                    UserProfileService userProfileService, OrganizationAccountRepository organizationAccountRepository, RoleRepository roleRepository, MembershipRepository membershipRepository) {
        this.membershipRetrievalService = membershipRetrievalService;
        this.organizationAccessService = organizationAccessService;
        this.userProfileService = userProfileService;
        this.organizationAccountRepository = organizationAccountRepository;
        this.roleRepository = roleRepository;
        this.membershipRepository = membershipRepository;
    }

    public UserProfileModel getOrganizationAccountOwner(Long organizationAccountId) {
        OrganizationAccountEntity orgAccountEntity = organizationAccountRepository.findById(organizationAccountId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        UserEntity authenticatedUserEntity = membershipRetrievalService.getAuthenticatedUserEntity();
        AccessLevel orgAccountAccessLevel = organizationAccessService.getAccessLevelForUserRequestingAccount(authenticatedUserEntity, orgAccountEntity);
        if (AccessLevel.NONE.equals(orgAccountAccessLevel)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        RoleEntity orgAccountOwnerRole = getOrgAccountOwnerRole();
        UserEntity orgAccountOwnerEntity = getUserEntity(orgAccountEntity, orgAccountOwnerRole);
        return userProfileService.retrieveProfileWithoutPermissionCheck(orgAccountOwnerEntity.getUserId());
    }

    public void updateOrganizationAccountOwner(Long organizationAccountId, NewAccountOwnerRequestModel updateModel) {
        // TODO implement

//        UserEntity orgAccountOwnerEntity = getOrgAccountOwnerEntity(organizationAccountId);
//        AccessLevel orgAccountOwnerAccessLevel = organizationAccessService.getAccessLevelForUserRequestingUser(authenticatedUserEntity, orgAccountOwnerEntity);
//        boolean hasUpdatePermission = AccessLevel.FULL.equals(orgAccountOwnerAccessLevel) || AccessLevel.READ_WRITE_INSENSITIVE.equals(orgAccountOwnerAccessLevel);
//        if (!hasUpdatePermission) {
//            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
//        }
    }

    private RoleEntity getOrgAccountOwnerRole() {
        return roleRepository.findFirstByRoleLevel(PortalAuthorityConstants.ORGANIZATION_ACCOUNT_OWNER)
                .<ResponseStatusException>orElseThrow(() -> {
                    log.error("Missing organization account owner role in database");
                    throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
                });
    }

    private UserEntity getUserEntity(OrganizationAccountEntity orgAccount, RoleEntity role) {
        List<MembershipEntity> orgAccountOwners = membershipRepository.findByRoleIdAndOrganizationAccountId(role.getRoleId(), orgAccount.getOrganizationAccountId());
        if (orgAccountOwners.size() != 1) {
            log.error("More than one org account owner for org account with id [{}]", orgAccount.getOrganizationAccountId());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return orgAccountOwners
                .stream()
                .findFirst()
                .map(membershipRetrievalService::getExistingUserFromMembership)
                .orElseThrow(() -> {
                    log.error("The org account owner was not present for org account with id [{}]", orgAccount.getOrganizationAccountId());
                    return new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
                });
    }

}
