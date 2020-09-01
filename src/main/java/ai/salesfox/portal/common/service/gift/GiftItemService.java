package ai.salesfox.portal.common.service.gift;

import ai.salesfox.integration.common.function.ThrowingConsumer;
import ai.salesfox.portal.database.account.entity.MembershipEntity;
import ai.salesfox.portal.database.account.entity.UserEntity;
import ai.salesfox.portal.database.gift.item.GiftItemDetailEntity;
import ai.salesfox.portal.database.inventory.InventoryRepository;
import ai.salesfox.portal.database.inventory.item.InventoryItemEntity;
import ai.salesfox.portal.database.inventory.item.InventoryItemPK;
import ai.salesfox.portal.database.inventory.item.InventoryItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class GiftItemService {
    private final InventoryRepository inventoryRepository;
    private final InventoryItemRepository inventoryItemRepository;

    @Autowired
    public GiftItemService(InventoryRepository inventoryRepository, InventoryItemRepository inventoryItemRepository) {
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
