package com.usepipeline.portal.web.inventory;

import com.usepipeline.portal.database.account.entity.MembershipEntity;
import com.usepipeline.portal.database.account.entity.UserEntity;
import com.usepipeline.portal.database.inventory.InventoryEntity;
import com.usepipeline.portal.web.util.HttpSafeUserMembershipRetrievalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class InventoryAccessService {
    private HttpSafeUserMembershipRetrievalService membershipRetrievalService;

    @Autowired
    public InventoryAccessService(HttpSafeUserMembershipRetrievalService membershipRetrievalService) {
        this.membershipRetrievalService = membershipRetrievalService;
    }

    public void validateInventoryAccess(InventoryEntity inventoryEntity) {
        if (membershipRetrievalService.isAuthenticatedUserPipelineAdmin()) {
            return;
        }

        UserEntity loggedInUser = membershipRetrievalService.getAuthenticatedUserEntity();
        if (inventoryEntity.getUserId() != null) {
            if (loggedInUser.getUserId().equals(inventoryEntity.getUserId())) {
                return;
            }
        } else {
            MembershipEntity userMembership = membershipRetrievalService.getMembershipEntity(loggedInUser);
            if (userMembership.getOrganizationAccountId().equals(inventoryEntity.getOrganizationAccountId())) {
                return;
            }
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN);
    }
    
}
