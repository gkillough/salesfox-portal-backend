package com.getboostr.portal.web.organization.common;

import com.getboostr.portal.common.enumeration.AccessOperation;
import com.getboostr.portal.database.account.entity.MembershipEntity;
import com.getboostr.portal.database.account.entity.UserEntity;
import com.getboostr.portal.database.organization.account.OrganizationAccountEntity;
import com.getboostr.portal.web.security.authorization.PortalAuthorityConstants;
import com.getboostr.portal.web.util.HttpSafeUserMembershipRetrievalService;
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
