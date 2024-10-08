package ai.salesfox.portal.rest.api.gift.util;

import ai.salesfox.portal.common.enumeration.AccessOperation;
import ai.salesfox.portal.common.service.contact.ContactAccessOperationUtility;
import ai.salesfox.portal.database.account.entity.MembershipEntity;
import ai.salesfox.portal.database.account.entity.UserEntity;
import ai.salesfox.portal.database.contact.OrganizationAccountContactRepository;
import ai.salesfox.portal.database.gift.GiftEntity;
import ai.salesfox.portal.database.gift.recipient.GiftRecipientEntity;
import ai.salesfox.portal.database.gift.recipient.GiftRecipientRepository;
import ai.salesfox.portal.database.gift.restriction.GiftOrgAccountRestrictionEntity;
import ai.salesfox.portal.database.gift.restriction.GiftUserRestrictionEntity;
import ai.salesfox.portal.database.inventory.InventoryEntity;
import ai.salesfox.portal.database.inventory.InventoryRepository;
import ai.salesfox.portal.database.inventory.item.InventoryItemPK;
import ai.salesfox.portal.database.inventory.item.InventoryItemRepository;
import ai.salesfox.portal.rest.api.inventory.InventoryAccessService;
import ai.salesfox.portal.rest.util.HttpSafeUserMembershipRetrievalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class GiftAccessService {
    private final HttpSafeUserMembershipRetrievalService membershipRetrievalService;
    private final ContactAccessOperationUtility contactAccessOperationUtility;
    private final GiftRecipientRepository giftRecipientRepository;
    private final InventoryRepository inventoryRepository;
    private final InventoryItemRepository inventoryItemRepository;
    private final InventoryAccessService inventoryAccessService;

    @Autowired
    public GiftAccessService(HttpSafeUserMembershipRetrievalService membershipRetrievalService, OrganizationAccountContactRepository contactRepository,
                             GiftRecipientRepository giftRecipientRepository, InventoryRepository inventoryRepository, InventoryItemRepository inventoryItemRepository, InventoryAccessService inventoryAccessService) {
        this.membershipRetrievalService = membershipRetrievalService;
        this.giftRecipientRepository = giftRecipientRepository;
        this.inventoryRepository = inventoryRepository;
        this.inventoryItemRepository = inventoryItemRepository;
        this.inventoryAccessService = inventoryAccessService;
        this.contactAccessOperationUtility = new ContactAccessOperationUtility(contactRepository);
    }

    public void validateGiftAccess(GiftEntity gift, UserEntity userRequestingAccess, AccessOperation contactAccessOperation) {
        validateGiftEntityAccess(gift, userRequestingAccess);

        Set<UUID> giftContactIds = giftRecipientRepository.findByGiftId(gift.getGiftId())
                .stream()
                .map(GiftRecipientEntity::getContactId)
                .collect(Collectors.toSet());
        if (!contactAccessOperationUtility.canUserAccessContacts(userRequestingAccess, giftContactIds, contactAccessOperation)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot perform the gifting operation for this contact");
        }
    }

    public void validateUserInventoryAccess(UserEntity userRequestingAccess, UUID itemId) {
        MembershipEntity userMembership = userRequestingAccess.getMembershipEntity();
        InventoryEntity userInventory = inventoryRepository.findAccessibleInventories(userMembership.getOrganizationAccountId(), PageRequest.of(0, 1))
                .stream()
                .findAny()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR));
        inventoryAccessService.validateInventoryAccess(userRequestingAccess, userInventory);

        InventoryItemPK inventoryItemPK = new InventoryItemPK(itemId, userInventory.getInventoryId());
        if (!inventoryItemRepository.existsById(inventoryItemPK)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The requested itemId is not in the requesting user's inventory");
        }
    }

    public void validateGiftEntityAccess(GiftEntity entity, UserEntity loggedInUser) {
        if (membershipRetrievalService.isAuthenticatedUserPortalAdmin()) {
            return;
        }

        GiftOrgAccountRestrictionEntity orgAcctRestriction = entity.getGiftOrgAccountRestrictionEntity();
        GiftUserRestrictionEntity userRestriction = entity.getGiftUserRestrictionEntity();

        MembershipEntity userMembership = loggedInUser.getMembershipEntity();
        if (null != orgAcctRestriction && orgAcctRestriction.getOrgAccountId().equals(userMembership.getOrganizationAccountId())) {
            return;
        } else if (null != userRestriction && userRestriction.getUserId().equals(loggedInUser.getUserId())) {
            return;
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN);
    }

}
