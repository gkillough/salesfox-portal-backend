package ai.salesfox.portal.rest.api.organization.common;

import ai.salesfox.portal.common.enumeration.AccessOperation;
import ai.salesfox.portal.database.account.entity.MembershipEntity;
import ai.salesfox.portal.database.account.entity.UserEntity;
import ai.salesfox.portal.database.organization.account.OrganizationAccountEntity;
import ai.salesfox.portal.rest.security.authorization.PortalAuthorityConstants;
import ai.salesfox.portal.rest.util.HttpSafeUserMembershipRetrievalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OrganizationAccessService {
    private HttpSafeUserMembershipRetrievalService membershipRetrievalService;

    @Autowired
    public OrganizationAccessService(HttpSafeUserMembershipRetrievalService membershipRetrievalService) {
        this.membershipRetrievalService = membershipRetrievalService;
    }

    public boolean canUserAccessOrganizationAccount(UserEntity requestingUser, OrganizationAccountEntity requestedAccount, AccessOperation requestedAccessOperation) {
        MembershipEntity userMembership = membershipRetrievalService.getMembershipEntity(requestingUser);
        String userRoleLevel = membershipRetrievalService.getRoleEntity(userMembership).getRoleLevel();

        if (PortalAuthorityConstants.PORTAL_ADMIN.equals(userRoleLevel)) {
            return true;
        } else if (userRoleLevel.startsWith(PortalAuthorityConstants.ORGANIZATION_ROLE_PREFIX) && userMembership.getOrganizationAccountId().equals(requestedAccount.getOrganizationAccountId())) {
            return canOrganizationUserAccessRequestedAccount(userRoleLevel, requestedAccessOperation);
        }
        return false;
    }

    private boolean canOrganizationUserAccessRequestedAccount(String userRoleLevel, AccessOperation requestedAccessOperation) {
        if (PortalAuthorityConstants.ORGANIZATION_ACCOUNT_OWNER.equals(userRoleLevel) || PortalAuthorityConstants.ORGANIZATION_ACCOUNT_MANAGER.equals(userRoleLevel)) {
            return true;
        }

        switch (requestedAccessOperation) {
            case READ:
                return true;
            default:
                return false;
        }
    }

}
