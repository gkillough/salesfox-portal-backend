package com.usepipeline.portal.web.organization.users;

import com.usepipeline.portal.common.enumeration.AccessLevel;
import com.usepipeline.portal.database.account.entity.MembershipEntity;
import com.usepipeline.portal.database.account.entity.RoleEntity;
import com.usepipeline.portal.database.account.entity.UserEntity;
import com.usepipeline.portal.database.account.repository.MembershipRepository;
import com.usepipeline.portal.database.account.repository.RoleRepository;
import com.usepipeline.portal.database.account.repository.UserRepository;
import com.usepipeline.portal.database.organization.account.OrganizationAccountEntity;
import com.usepipeline.portal.database.organization.account.OrganizationAccountRepository;
import com.usepipeline.portal.web.common.model.ActiveStatusPatchModel;
import com.usepipeline.portal.web.organization.common.OrganizationAccessService;
import com.usepipeline.portal.web.organization.users.model.NewAccountOwnerRequestModel;
import com.usepipeline.portal.web.organization.users.model.OrganizationMultiUsersModel;
import com.usepipeline.portal.web.security.authorization.PortalAuthorityConstants;
import com.usepipeline.portal.web.user.active.UserActiveService;
import com.usepipeline.portal.web.user.common.UserAccessService;
import com.usepipeline.portal.web.user.common.model.UserAccountModel;
import com.usepipeline.portal.web.user.profile.UserProfileService;
import com.usepipeline.portal.web.user.profile.model.UserProfileModel;
import com.usepipeline.portal.web.user.role.model.UserRoleModel;
import com.usepipeline.portal.web.util.HttpSafeUserMembershipRetrievalService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import javax.transaction.Transactional;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
public class OrganizationUsersService {
    private HttpSafeUserMembershipRetrievalService membershipRetrievalService;
    private OrganizationAccessService organizationAccessService;
    private UserProfileService userProfileService;
    private UserAccessService userAccessService;
    private UserActiveService userActiveService;
    private OrganizationAccountRepository organizationAccountRepository;
    private RoleRepository roleRepository;
    private MembershipRepository membershipRepository;
    private UserRepository userRepository;

    @Autowired
    public OrganizationUsersService(HttpSafeUserMembershipRetrievalService membershipRetrievalService, OrganizationAccessService organizationAccessService, UserProfileService userProfileService, UserAccessService userAccessService,
                                    UserActiveService userActiveService, OrganizationAccountRepository organizationAccountRepository, RoleRepository roleRepository, MembershipRepository membershipRepository, UserRepository userRepository) {
        this.membershipRetrievalService = membershipRetrievalService;
        this.organizationAccessService = organizationAccessService;
        this.userProfileService = userProfileService;
        this.userAccessService = userAccessService;
        this.userActiveService = userActiveService;
        this.organizationAccountRepository = organizationAccountRepository;
        this.roleRepository = roleRepository;
        this.membershipRepository = membershipRepository;
        this.userRepository = userRepository;
    }

    public UserAccountModel getOrganizationAccountUser(Long organizationAccountId, Long userId) {
        OrganizationAccountEntity orgAccountEntity = organizationAccountRepository.findById(organizationAccountId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        UserEntity requestedUser = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        UserEntity authenticatedUserEntity = membershipRetrievalService.getAuthenticatedUserEntity();

        validateUserHasAccessToOrgAccount(authenticatedUserEntity, orgAccountEntity);
        validateUserIsAMemberOfOrgAccount(organizationAccountId, requestedUser);

        return convertToAccountModel(requestedUser);
    }

    public void setOrganizationAccountUserActiveStatus(Long organizationAccountId, Long userId, ActiveStatusPatchModel updateModel) {
        OrganizationAccountEntity orgAccountEntity = organizationAccountRepository.findById(organizationAccountId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        UserEntity userToBeUpdated = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        UserEntity authenticatedUserEntity = membershipRetrievalService.getAuthenticatedUserEntity();

        validateUserHasAccessToOrgAccount(authenticatedUserEntity, orgAccountEntity);
        validateUserIsAMemberOfOrgAccount(organizationAccountId, userToBeUpdated);

        // Ensure that if the org account owner active status is being changed, that it is either by the org account owner or a pipeline admin.
        RoleEntity orgAccountOwnerRole = getExistingRole(PortalAuthorityConstants.ORGANIZATION_ACCOUNT_OWNER);
        UserEntity orgAccountOwnerEntity = getOrganizationAccountOwnerEntity(orgAccountEntity, orgAccountOwnerRole);
        if (orgAccountOwnerEntity.getUserId().equals(userId) && !authenticatedUserEntity.getUserId().equals(userId) && !membershipRetrievalService.isPipelineAdmin()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        userActiveService.updateUserActiveStatusWithoutPermissionCheck(userId, updateModel.getActiveStatus());
    }

    public OrganizationMultiUsersModel getOrganizationAccountUsers(Long organizationAccountId) {
        OrganizationAccountEntity orgAccountEntity = organizationAccountRepository.findById(organizationAccountId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        UserEntity authenticatedUserEntity = membershipRetrievalService.getAuthenticatedUserEntity();
        validateUserHasAccessToOrgAccount(authenticatedUserEntity, orgAccountEntity);

        Set<Long> orgAccountUserIds = membershipRepository.findByOrganizationAccountId(organizationAccountId)
                .stream()
                .map(MembershipEntity::getUserId)
                .collect(Collectors.toSet());

        List<UserAccountModel> orgAccountUserAccountModels = userRepository.findAllById(orgAccountUserIds)
                .stream()
                .map(this::convertToAccountModel)
                .collect(Collectors.toList());

        return new OrganizationMultiUsersModel(orgAccountUserAccountModels);
    }

    public UserProfileModel getOrganizationAccountOwner(Long organizationAccountId) {
        OrganizationAccountEntity orgAccountEntity = organizationAccountRepository.findById(organizationAccountId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        UserEntity authenticatedUserEntity = membershipRetrievalService.getAuthenticatedUserEntity();
        validateUserHasAccessToOrgAccount(authenticatedUserEntity, orgAccountEntity);

        RoleEntity orgAccountOwnerRole = getExistingRole(PortalAuthorityConstants.ORGANIZATION_ACCOUNT_OWNER);
        UserEntity orgAccountOwnerEntity = getOrganizationAccountOwnerEntity(orgAccountEntity, orgAccountOwnerRole);
        return userProfileService.retrieveProfileWithoutPermissionCheck(orgAccountOwnerEntity.getUserId());
    }

    @Transactional
    public void transferOrganizationAccountOwnership(Long organizationAccountId, NewAccountOwnerRequestModel updateModel) {
        OrganizationAccountEntity orgAccountEntity = organizationAccountRepository.findById(organizationAccountId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        UserEntity authenticatedUserEntity = membershipRetrievalService.getAuthenticatedUserEntity();
        validateUserHasAccessToOrgAccount(authenticatedUserEntity, orgAccountEntity);

        if (StringUtils.isBlank(updateModel.getEmail())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The 'email' field cannot be blank");
        }

        UserEntity candidateOrgAcctOwner = userRepository.findFirstByEmail(updateModel.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("No user with the email ['%s'] exists", updateModel.getEmail())));

        MembershipEntity candidateOrgAcctOwnerMembership = membershipRetrievalService.getMembershipEntity(candidateOrgAcctOwner);
        if (!candidateOrgAcctOwnerMembership.getOrganizationAccountId().equals(organizationAccountId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("The user with the email ['%s'] is not a member of the Organization Account", updateModel.getEmail()));
        }

        RoleEntity roleEntity = membershipRetrievalService.getRoleEntity(candidateOrgAcctOwnerMembership);
        if (!roleEntity.getRoleLevel().startsWith(PortalAuthorityConstants.ORGANIZATION_ROLE_PREFIX)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("The user with the email ['%s'] does not have a valid role in the Organization Account", updateModel.getEmail()));
        }

        RoleEntity orgAcctOwnerRole = getExistingRole(PortalAuthorityConstants.ORGANIZATION_ACCOUNT_OWNER);
        RoleEntity orgAcctManagerRole = getExistingRole(PortalAuthorityConstants.ORGANIZATION_ACCOUNT_MANAGER);

        UserEntity currentOrgAcctOwner = getOrganizationAccountOwnerEntity(orgAccountEntity, orgAcctOwnerRole);
        MembershipEntity currentOrgAcctOwnerMembership = membershipRetrievalService.getMembershipEntity(currentOrgAcctOwner);

        candidateOrgAcctOwnerMembership.setRoleId(orgAcctOwnerRole.getRoleId());
        currentOrgAcctOwnerMembership.setRoleId(orgAcctManagerRole.getRoleId());

        List<MembershipEntity> membershipsToSave = Arrays.asList(candidateOrgAcctOwnerMembership, currentOrgAcctOwnerMembership);
        membershipRepository.saveAll(membershipsToSave);

        if (authenticatedUserEntity.getUserId().equals(currentOrgAcctOwner.getUserId())) {
            // If the "old" org account owner is the one making the request, his/her session will still have the role until logging out.
            clearOldOrgAccountOwnerSession();
        }
    }

    private void validateUserHasAccessToOrgAccount(UserEntity authenticatedUserEntity, OrganizationAccountEntity orgAccountEntity) {
        AccessLevel orgAccountAccessLevel = organizationAccessService.getAccessLevelForUserRequestingAccount(authenticatedUserEntity, orgAccountEntity);
        if (AccessLevel.NONE.equals(orgAccountAccessLevel)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
    }

    private void validateUserIsAMemberOfOrgAccount(Long organizationAccountId, UserEntity userEntity) {
        MembershipEntity requestedUserMembership = membershipRetrievalService.getMembershipEntity(userEntity);
        if (!requestedUserMembership.getOrganizationAccountId().equals(organizationAccountId)) {
            // The user exists, but is not a member of the organization account. To prevent any unnecessary details about
            // the user's account being leaked, treat this the as if the user does not exist.
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    private RoleEntity getExistingRole(String roleLevel) {
        return roleRepository.findFirstByRoleLevel(roleLevel)
                .<ResponseStatusException>orElseThrow(() -> {
                    log.error("Missing role with level [{}] in database", roleLevel);
                    throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
                });
    }

    private UserEntity getOrganizationAccountOwnerEntity(OrganizationAccountEntity orgAccount, RoleEntity role) {
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

    private UserAccountModel convertToAccountModel(UserEntity userEntity) {
        UserRoleModel userRole = userAccessService.findRoleByUserId(userEntity.getUserId());
        return new UserAccountModel(userEntity.getFirstName(), userEntity.getLastName(), userEntity.getEmail(), userRole, userEntity.getIsActive());
    }

    private void clearOldOrgAccountOwnerSession() {
        SecurityContextHolder.clearContext();
    }

}
