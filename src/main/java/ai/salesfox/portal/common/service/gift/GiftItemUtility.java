package ai.salesfox.portal.common.service.gift;

import ai.salesfox.portal.common.exception.ThrowingConsumer;
import ai.salesfox.portal.database.account.entity.MembershipEntity;
import ai.salesfox.portal.database.account.entity.UserEntity;
import ai.salesfox.portal.database.gift.item.GiftItemDetailEntity;
import ai.salesfox.portal.database.inventory.InventoryRepository;
import ai.salesfox.portal.database.inventory.item.InventoryItemEntity;
import ai.salesfox.portal.database.inventory.item.InventoryItemPK;
import ai.salesfox.portal.database.inventory.item.InventoryItemRepository;
import org.springframework.data.domain.PageRequest;

import java.util.Optional;

public class GiftItemUtility {
    private final InventoryRepository inventoryRepository;
    private final InventoryItemRepository inventoryItemRepository;

    public GiftItemUtility(InventoryRepository inventoryRepository, InventoryItemRepository inventoryItemRepository) {
        this.inventoryRepository = inventoryRepository;
        this.inventoryItemRepository = inventoryItemRepository;
    }

    public Optional<InventoryItemEntity> findInventoryItemForGift(UserEntity loggedInUser, MembershipEntity userMembership, GiftItemDetailEntity giftItemDetail) {
        return inventoryRepository.findAccessibleInventories(userMembership.getOrganizationAccountId(), loggedInUser.getUserId(), PageRequest.of(0, 1))
                .stream()
                .findAny()
                .map(inventory -> new InventoryItemPK(giftItemDetail.getItemId(), inventory.getInventoryId()))
                .flatMap(inventoryItemRepository::findById);
    }

    public void incrementItemQuantity(InventoryItemEntity inventoryItemForGift) {
        inventoryItemForGift.setQuantity(inventoryItemForGift.getQuantity() + 1L);
        inventoryItemRepository.save(inventoryItemForGift);
    }

    public <E extends Throwable> void decrementItemQuantityOrElse(InventoryItemEntity inventoryItemForGift, ThrowingConsumer<InventoryItemEntity, E> outOfStockHandler) throws E {
        if (inventoryItemForGift.getQuantity() < 1L) {
            outOfStockHandler.accept(inventoryItemForGift);
        } else {
            inventoryItemForGift.setQuantity(inventoryItemForGift.getQuantity() - 1L);
            inventoryItemRepository.save(inventoryItemForGift);
        }
    }

}
