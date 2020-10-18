package ai.salesfox.portal.rest.api.inventory;

import ai.salesfox.portal.database.account.entity.MembershipEntity;
import ai.salesfox.portal.database.account.entity.UserEntity;
import ai.salesfox.portal.database.inventory.InventoryEntity;
import ai.salesfox.portal.database.inventory.restriction.InventoryOrganizationAccountRestrictionEntity;
import ai.salesfox.portal.rest.util.HttpSafeUserMembershipRetrievalService;
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
            if (orgAcctRestriction != null && orgAcctRestriction.getOrganizationAccountId().equals(userMembership.getOrganizationAccountId())) {
                return;
            }
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN);
    }

}
