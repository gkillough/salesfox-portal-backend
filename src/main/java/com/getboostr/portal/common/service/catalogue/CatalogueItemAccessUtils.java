package com.getboostr.portal.common.service.catalogue;

import com.getboostr.portal.database.account.entity.MembershipEntity;
import com.getboostr.portal.database.account.entity.UserEntity;
import com.getboostr.portal.database.catalogue.item.CatalogueItemEntity;
import com.getboostr.portal.database.catalogue.restriction.CatalogueItemOrganizationAccountRestrictionEntity;
import com.getboostr.portal.database.catalogue.restriction.CatalogueItemUserRestrictionEntity;
import com.getboostr.portal.rest.security.authorization.PortalAuthorityConstants;

public class CatalogueItemAccessUtils {
    public static boolean doesUserHaveItemAccess(UserEntity userRequestingAccess, CatalogueItemEntity itemEntity) {
        if (!itemEntity.hasRestriction()) {
            return true;
        }

        MembershipEntity userMembership = userRequestingAccess.getMembershipEntity();
        String userRoleLevel = userMembership.getRoleEntity().getRoleLevel();
        if (PortalAuthorityConstants.PORTAL_ADMIN.equals(userRoleLevel)) {
            return true;
        }

        CatalogueItemOrganizationAccountRestrictionEntity orgAcctRestriction = itemEntity.getCatalogueItemOrganizationAccountRestrictionEntity();
        if (null != orgAcctRestriction && userMembership.getOrganizationAccountId().equals(orgAcctRestriction.getOrganizationAccountId())) {
            return true;
        }

        CatalogueItemUserRestrictionEntity userRestriction = itemEntity.getCatalogueItemUserRestrictionEntity();
        if (null != userRestriction && userRequestingAccess.getUserId().equals(userRestriction.getUserId())) {
            return true;
        }
        return false;
    }

}
