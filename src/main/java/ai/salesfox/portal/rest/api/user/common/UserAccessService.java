package ai.salesfox.portal.rest.api.user.common;

import ai.salesfox.portal.database.account.entity.MembershipEntity;
import ai.salesfox.portal.database.account.entity.RoleEntity;
import ai.salesfox.portal.database.account.entity.UserEntity;
import ai.salesfox.portal.database.account.repository.MembershipRepository;
import ai.salesfox.portal.database.account.repository.UserRepository;
import ai.salesfox.portal.database.organization.account.OrganizationAccountEntity;
import ai.salesfox.portal.rest.api.user.role.model.UserRoleModel;
import ai.salesfox.portal.rest.security.authorization.PortalAuthorityConstants;
import ai.salesfox.portal.rest.util.HttpSafeUserMembershipRetrievalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Component
public class UserAccessService {
    private final UserRepository userRepository;
    private final MembershipRepository membershipRepository;
    private final HttpSafeUserMembershipRetrievalService membershipRetrievalService;

    @Autowired
    public UserAccessService(UserRepository userRepository, MembershipRepository membershipRepository, HttpSafeUserMembershipRetrievalService membershipRetrievalService) {
        this.userRepository = userRepository;
        this.membershipRepository = membershipRepository;
        this.membershipRetrievalService = membershipRetrievalService;
    }

    @Deprecated
    public boolean canCurrentUserAccessDataForUser(UUID userId) {
        UserEntity authenticatedUser = membershipRetrievalService.getAuthenticatedUserEntity();
        UserEntity targetUser = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        return canUserAccessDataForUser(authenticatedUser, targetUser);
    }

    public boolean canUserAccessDataForUser(UserEntity requestingUser, UserEntity targetUser) {
        MembershipEntity requestingUserMembership = requestingUser.getMembershipEntity();
        RoleEntity requestingUserRole = requestingUserMembership.getRoleEntity();
        String requestingUserRoleLevel = requestingUserRole.getRoleLevel();
        if (PortalAuthorityConstants.PORTAL_ADMIN.equals(requestingUserRoleLevel)) {
            return true;
        }

        MembershipEntity targetUserMembership = targetUser.getMembershipEntity();
        OrganizationAccountEntity targetUserOrgAcct = targetUserMembership.getOrganizationAccountEntity();

        OrganizationAccountEntity requestingUserOrgAcct = requestingUserMembership.getOrganizationAccountEntity();
        return requestingUserOrgAcct.getOrganizationAccountId().equals(targetUserOrgAcct.getOrganizationAccountId())
                && (PortalAuthorityConstants.ORGANIZATION_ACCOUNT_OWNER.equals(requestingUserRoleLevel)
                || PortalAuthorityConstants.ORGANIZATION_ACCOUNT_MANAGER.equals(requestingUserRoleLevel));
    }

    @Deprecated
    public UserRoleModel findRoleByUserId(UUID userId) {
        return membershipRepository.findById(userId)
                .map(MembershipEntity::getRoleEntity)
                .map(roleEntity -> new UserRoleModel(roleEntity.getRoleLevel(), roleEntity.getDescription()))
                .orElse(UserRoleModel.ANONYMOUS_ROLE);
    }

}
