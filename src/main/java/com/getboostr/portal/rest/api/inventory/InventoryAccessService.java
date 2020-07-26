package com.getboostr.portal.rest.api.inventory;

import com.getboostr.portal.database.account.entity.MembershipEntity;
import com.getboostr.portal.database.account.entity.UserEntity;
import com.getboostr.portal.database.inventory.InventoryEntity;
import com.getboostr.portal.database.inventory.restriction.InventoryOrganizationAccountRestrictionEntity;
import com.getboostr.portal.database.inventory.restriction.InventoryUserRestrictionEntity;
import com.getboostr.portal.rest.util.HttpSafeUserMembershipRetrievalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class InventoryAccessService {
    private final HttpSafeUserMembershipRetrievalService membershipRetrievalService;

    @Autowired
    public InventoryAccessService(HttpSafeUserMembershipRetrievalService membershipRetrievalService) {
        this.membershipRetrievalService = membershipRetrievalService;
    }

    public void validateInventoryAccess(InventoryEntity inventoryEntity) {
        UserEntity loggedInUser = membershipRetrievalService.getAuthenticatedUserEntity();
        validateInventoryAccess(loggedInUser, inventoryEntity);
    }

    public void validateInventoryAccess(UserEntity requestingUser, InventoryEntity inventoryEntity) {
        if (membershipRetrievalService.isAuthenticatedUserPortalAdmin()) {
            return;
        }

        if (inventoryEntity.hasRestriction()) {
            MembershipEntity userMembership = requestingUser.getMembershipEntity();
            InventoryOrganizationAccountRestrictionEntity orgAcctRestriction = inventoryEntity.getInventoryOrganizationAccountRestrictionEntity();
            InventoryUserRestrictionEntity userRestriction = inventoryEntity.getInventoryUserRestrictionEntity();
            if (orgAcctRestriction != null && orgAcctRestriction.getOrganizationAccountId().equals(userMembership.getOrganizationAccountId())) {
                return;
            } else if (userRestriction != null && userRestriction.getUserId().equals(requestingUser.getUserId())) {
                return;
            }
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN);
    }

}
