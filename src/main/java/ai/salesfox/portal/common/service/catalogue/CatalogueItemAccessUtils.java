package ai.salesfox.portal.common.service.catalogue;

import ai.salesfox.portal.database.account.entity.MembershipEntity;
import ai.salesfox.portal.database.account.entity.UserEntity;
import ai.salesfox.portal.database.catalogue.item.CatalogueItemEntity;
import ai.salesfox.portal.database.catalogue.restriction.CatalogueItemOrganizationAccountRestrictionEntity;
import ai.salesfox.portal.database.catalogue.restriction.CatalogueItemUserRestrictionEntity;
import ai.salesfox.portal.rest.security.authorization.PortalAuthorityConstants;

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
