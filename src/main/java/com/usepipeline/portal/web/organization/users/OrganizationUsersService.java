package com.usepipeline.portal.web.organization.users;

import com.usepipeline.portal.common.enumeration.AccessOperation;
import com.usepipeline.portal.database.account.entity.LoginEntity;
import com.usepipeline.portal.database.account.entity.MembershipEntity;
import com.usepipeline.portal.database.account.entity.RoleEntity;
import com.usepipeline.portal.database.account.entity.UserEntity;
import com.usepipeline.portal.database.account.repository.LoginRepository;
import com.usepipeline.portal.database.account.repository.MembershipRepository;
import com.usepipeline.portal.database.account.repository.RoleRepository;
import com.usepipeline.portal.database.account.repository.UserRepository;
import com.usepipeline.portal.database.organization.account.OrganizationAccountEntity;
import com.usepipeline.portal.database.organization.account.OrganizationAccountRepository;
import com.usepipeline.portal.web.common.model.request.ActiveStatusPatchModel;
import com.usepipeline.portal.web.common.page.PageRequestValidationUtils;
import com.usepipeline.portal.web.organization.common.OrganizationAccessService;
import com.usepipeline.portal.web.organization.users.model.OrganizationMultiUsersModel;
import com.usepipeline.portal.web.organization.users.model.OrganizationUserAdminViewModel;
import com.usepipeline.portal.web.security.authentication.user.PortalUserDetailsService;
import com.usepipeline.portal.web.security.authorization.PortalAuthorityConstants;
import com.usepipeline.portal.web.user.active.UserActiveService;
import com.usepipeline.portal.web.user.common.model.UserLoginInfoModel;
import com.usepipeline.portal.web.user.profile.UserProfileService;
import com.usepipeline.portal.web.user.profile.model.UserProfileModel;
import com.usepipeline.portal.web.user.role.model.UserRoleModel;
import com.usepipeline.portal.web.util.HttpSafeUserMembershipRetrievalService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Component
public class OrganizationUsersService {
    private HttpSafeUserMembershipRetrievalService membershipRetrievalService;
    private OrganizationAccessService organizationAccessService;
    private UserProfileService userProfileService;
    private UserActiveService userActiveService;
    private PortalUserDetailsService portalUserDetailsService;
    private OrganizationAccountRepository organizationAccountRepository;
    private RoleRepository roleRepository;
    private MembershipRepository membershipRepository;
    private UserRepository userRepository;
    private LoginRepository loginRepository;

    @Autowired
    public OrganizationUsersService(HttpSafeUserMembershipRetrievalService membershipRetrievalService, OrganizationAccessService organizationAccessService, UserProfileService userProfileService, UserActiveService userActiveService,
                                    PortalUserDetailsService portalUserDetailsService, OrganizationAccountRepository organizationAccountRepository, RoleRepository roleRepository, MembershipRepository membershipRepository, UserRepository userRepository, LoginRepository loginRepository) {
        this.membershipRetrievalService = membershipRetrievalService;
        this.organizationAccessService = organizationAccessService;
        this.userProfileService = userProfileService;
        this.userActiveService = userActiveService;
        this.portalUserDetailsService = portalUserDetailsService;
        this.organizationAccountRepository = organizationAccountRepository;
        this.roleRepository = roleRepository;
        this.membershipRepository = membershipRepository;
        this.userRepository = userRepository;
        this.loginRepository = loginRepository;
    }

    public OrganizationUserAdminViewModel getOrganizationAccountUser(UUID organizationAccountId, UUID userId) {
        OrganizationAccountEntity orgAccountEntity = organizationAccountRepository.findById(organizationAccountId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        UserEntity requestedUser = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        UserEntity authenticatedUserEntity = membershipRetrievalService.getAuthenticatedUserEntity();

        validateUserHasAccessToOrgAccount(authenticatedUserEntity, orgAccountEntity, AccessOperation.READ);
        validateUserIsAMemberOfOrgAccount(organizationAccountId, requestedUser);

        return convertToAdminViewModel(requestedUser, new UserRoleModelCache(roleRepository), true);
    }

    public OrganizationMultiUsersModel getOrganizationAccountUsers(UUID organizationAccountId, Integer pageOffset, Integer pageLimit) {
        PageRequestValidationUtils.validatePagingParams(pageOffset, pageLimit);
        OrganizationAccountEntity orgAccountEntity = organizationAccountRepository.findById(organizationAccountId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        UserEntity authenticatedUserEntity = membershipRetrievalService.getAuthenticatedUserEntity();
        validateUserHasAccessToOrgAccount(authenticatedUserEntity, orgAccountEntity, AccessOperation.READ);

        PageRequest pageRequest = PageRequest.of(pageOffset, pageLimit);
        Page<MembershipEntity> orgAccountMembershipsPage = membershipRepository.findByOrganizationAccountId(organizationAccountId, pageRequest);
        List<UUID> orgAccountUserIds = orgAccountMembershipsPage
                .stream()
                .map(MembershipEntity::getUserId)
                .collect(Collectors.toList());

        UserRoleModelCache roleCache = new UserRoleModelCache(roleRepository);
        List<OrganizationUserAdminViewModel> orgAccountUserAccountModels = userRepository.findAllById(orgAccountUserIds)
                .stream()
                .map(user -> convertToAdminViewModel(user, roleCache, false))
                .collect(Collectors.toList());

        return new OrganizationMultiUsersModel(orgAccountUserAccountModels, orgAccountMembershipsPage);
    }

    public void setOrganizationAccountUserActiveStatus(UUID organizationAccountId, UUID userId, ActiveStatusPatchModel updateModel) {
        OrganizationAccountEntity orgAccountEntity = organizationAccountRepository.findById(organizationAccountId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        UserEntity userToBeUpdated = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        UserEntity authenticatedUserEntity = membershipRetrievalService.getAuthenticatedUserEntity();

        validateUserHasAccessToOrgAccount(authenticatedUserEntity, orgAccountEntity, AccessOperation.UPDATE);
        validateUserIsAMemberOfOrgAccount(organizationAccountId, userToBeUpdated);

        RoleEntity orgAccountOwnerRole = getExistingRole(PortalAuthorityConstants.ORGANIZATION_ACCOUNT_OWNER);
        UserEntity orgAccountOwnerEntity = getOrganizationAccountOwnerEntity(orgAccountEntity, orgAccountOwnerRole);
        if (orgAccountOwnerEntity.getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The Organization Account Owner's active status cannot be changed");
        }

        userActiveService.updateUserActiveStatusWithoutPermissionCheck(userId, updateModel.getActiveStatus());
    }

    public void unlockOrganizationAccountUser(UUID organizationAccountId, UUID userId) {
        OrganizationAccountEntity orgAccountEntity = organizationAccountRepository.findById(organizationAccountId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        UserEntity userToBeUnlocked = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        UserEntity authenticatedUserEntity = membershipRetrievalService.getAuthenticatedUserEntity();
        validateUserHasAccessToOrgAccount(authenticatedUserEntity, orgAccountEntity, AccessOperation.UPDATE);
        validateUserIsAMemberOfOrgAccount(organizationAccountId, userToBeUnlocked);

        LoginEntity loginEntity = loginRepository.findFirstByUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR));
        boolean isUserLocked = portalUserDetailsService.isUserLocked(loginEntity);
        if (isUserLocked) {
            loginEntity.setLastLocked(null);
            loginRepository.save(loginEntity);
        }
    }

    private void validateUserHasAccessToOrgAccount(UserEntity authenticatedUserEntity, OrganizationAccountEntity orgAccountEntity, AccessOperation requestedAccessOperation) {
        boolean canAccess = organizationAccessService.canUserAccessOrganizationAccount(authenticatedUserEntity, orgAccountEntity, requestedAccessOperation);
        if (!canAccess) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
    }

    private void validateUserIsAMemberOfOrgAccount(UUID organizationAccountId, UserEntity userEntity) {
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

    private OrganizationUserAdminViewModel convertToAdminViewModel(UserEntity userEntity, UserRoleModelCache roleCache, boolean includeLoginInfo) {
        UserProfileModel userProfileModel = userProfileService.retrieveProfileWithoutPermissionCheck(userEntity.getUserId());
        MembershipEntity membershipEntity = membershipRetrievalService.getMembershipEntity(userEntity);
        UserRoleModel userRole = roleCache.findRoleByIdAndConvertToModel(membershipEntity.getRoleId());

        UserLoginInfoModel loginInfo = null;
        if (includeLoginInfo) {
            LoginEntity loginEntity = loginRepository.findFirstByUserId(userEntity.getUserId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR));
            boolean isUserLocked = portalUserDetailsService.isUserLocked(loginEntity);
            loginInfo = new UserLoginInfoModel(loginEntity.getLastSuccessfulLogin(), loginEntity.getLastLocked(), isUserLocked);
        }

        return OrganizationUserAdminViewModel.fromProfile(userEntity.getUserId(), userProfileModel, userEntity.getIsActive(), userRole, loginInfo);
    }

    private static class UserRoleModelCache {
        private RoleRepository roleRepository;
        private Map<UUID, UserRoleModel> roleIdToModelMap = new HashMap<>();

        private UserRoleModelCache(RoleRepository roleRepository) {
            this.roleRepository = roleRepository;
        }

        public UserRoleModel findRoleByIdAndConvertToModel(UUID roleId) {
            UserRoleModel userRoleModel = roleIdToModelMap.computeIfAbsent(roleId, ignored ->
                    roleRepository.findById(roleId)
                            .map(role -> new UserRoleModel(role.getRoleLevel(), role.getDescription()))
                            .orElse(null)
            );

            if (userRoleModel != null) {
                return userRoleModel;
            }
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

}
