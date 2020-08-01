package com.getboostr.portal.rest.api.gift.util;

import com.getboostr.portal.common.enumeration.AccessOperation;
import com.getboostr.portal.common.service.contact.ContactAccessOperationUtility;
import com.getboostr.portal.database.account.entity.MembershipEntity;
import com.getboostr.portal.database.account.entity.UserEntity;
import com.getboostr.portal.database.contact.OrganizationAccountContactEntity;
import com.getboostr.portal.database.contact.OrganizationAccountContactRepository;
import com.getboostr.portal.database.contact.profile.OrganizationAccountContactProfileRepository;
import com.getboostr.portal.database.gift.GiftEntity;
import com.getboostr.portal.database.inventory.InventoryEntity;
import com.getboostr.portal.database.inventory.InventoryRepository;
import com.getboostr.portal.database.inventory.item.InventoryItemPK;
import com.getboostr.portal.database.inventory.item.InventoryItemRepository;
import com.getboostr.portal.rest.api.inventory.InventoryAccessService;
import com.getboostr.portal.rest.util.HttpSafeUserMembershipRetrievalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;

@Component
public class GiftAccessService {
    private final HttpSafeUserMembershipRetrievalService membershipRetrievalService;
    private final OrganizationAccountContactRepository contactRepository;
    private final ContactAccessOperationUtility<ResponseStatusException> contactAccessOperationUtility;
    private final InventoryRepository inventoryRepository;
    private final InventoryItemRepository inventoryItemRepository;
    private final InventoryAccessService inventoryAccessService;

    @Autowired
    public GiftAccessService(HttpSafeUserMembershipRetrievalService membershipRetrievalService, OrganizationAccountContactRepository contactRepository, OrganizationAccountContactProfileRepository contactProfileRepository,
                             InventoryRepository inventoryRepository, InventoryItemRepository inventoryItemRepository, InventoryAccessService inventoryAccessService) {
        this.membershipRetrievalService = membershipRetrievalService;
        this.contactRepository = contactRepository;
        this.inventoryRepository = inventoryRepository;
        this.inventoryItemRepository = inventoryItemRepository;
        this.inventoryAccessService = inventoryAccessService;
        this.contactAccessOperationUtility = new ContactAccessOperationUtility<>(membershipRetrievalService, contactProfileRepository);
    }

    public void validateGiftAccess(GiftEntity entity, AccessOperation contactAccessOperation) {
        UserEntity loggedInUser = membershipRetrievalService.getAuthenticatedUserEntity();
        validateGiftEntityAccess(entity, loggedInUser);
        Optional<OrganizationAccountContactEntity> optionalContact = contactRepository.findById(entity.getContactId());
        if (optionalContact.isPresent()) {
            if (!contactAccessOperationUtility.canUserAccessContact(loggedInUser, optionalContact.get(), contactAccessOperation)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot perform the gifting operation for this contact");
            }
        }
    }

    public void validateUserGiftSendingAccessForContact(UserEntity userRequestingAccess, OrganizationAccountContactEntity contact) {
        if (!contactAccessOperationUtility.canUserAccessContact(userRequestingAccess, contact, AccessOperation.INTERACT)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot send gifts to this contact");
        }
    }

    public void validateUserInventoryAccess(UserEntity userRequestingAccess, UUID itemId) {
        MembershipEntity userMembership = userRequestingAccess.getMembershipEntity();
        InventoryEntity userInventory = inventoryRepository.findAccessibleInventories(userMembership.getOrganizationAccountId(), userRequestingAccess.getUserId(), PageRequest.of(1, 1))
                .stream()
                .findAny()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR));
        inventoryAccessService.validateInventoryAccess(userRequestingAccess, userInventory);

        InventoryItemPK inventoryItemPK = new InventoryItemPK(itemId, userInventory.getInventoryId());
        if (!inventoryItemRepository.existsById(inventoryItemPK)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The requested itemId is not in the requesting user's inventory");
        }
    }

    private void validateGiftEntityAccess(GiftEntity entity, UserEntity loggedInUser) {
        if (membershipRetrievalService.isAuthenticatedUserPortalAdmin()) {
            return;
        }

        if (membershipRetrievalService.isAuthenticateUserBasicOrPremiumMember()) {
            if (loggedInUser.getUserId().equals(entity.getRequestingUserId())) {
                return;
            }
        } else {
            MembershipEntity userMembership = loggedInUser.getMembershipEntity();
            if (userMembership.getOrganizationAccountId().equals(entity.getOrganizationAccountId())) {
                return;
            }
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN);
    }

}
