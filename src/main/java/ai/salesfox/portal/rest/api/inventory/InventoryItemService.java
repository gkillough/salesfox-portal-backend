package ai.salesfox.portal.rest.api.inventory;

import ai.salesfox.portal.rest.api.inventory.model.InventoryItemResponseModel;
import ai.salesfox.portal.rest.api.inventory.model.MultiInventoryItemModel;
import ai.salesfox.portal.database.catalogue.item.CatalogueItemEntity;
import ai.salesfox.portal.database.inventory.InventoryEntity;
import ai.salesfox.portal.database.inventory.InventoryRepository;
import ai.salesfox.portal.database.inventory.item.InventoryItemEntity;
import ai.salesfox.portal.database.inventory.item.InventoryItemPK;
import ai.salesfox.portal.database.inventory.item.InventoryItemRepository;
import ai.salesfox.portal.rest.api.common.page.PageRequestValidationUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class InventoryItemService {
    private final InventoryRepository inventoryRepository;
    private final InventoryItemRepository inventoryItemRepository;
    private final InventoryAccessService inventoryAccessService;

    @Autowired
    public InventoryItemService(InventoryRepository inventoryRepository, InventoryItemRepository inventoryItemRepository, InventoryAccessService inventoryAccessService) {
        this.inventoryRepository = inventoryRepository;
        this.inventoryItemRepository = inventoryItemRepository;
        this.inventoryAccessService = inventoryAccessService;
    }

    public MultiInventoryItemModel getInventoryItems(UUID inventoryId, Integer pageOffset, Integer pageLimit) {
        InventoryEntity foundInventory = inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        inventoryAccessService.validateInventoryAccess(foundInventory);
        PageRequestValidationUtils.validatePagingParams(pageOffset, pageLimit);

        PageRequest pageRequest = PageRequest.of(pageOffset, pageLimit);
        Page<InventoryItemEntity> pageOfInventoryItems = inventoryItemRepository.findByInventoryId(inventoryId, pageRequest);
        List<InventoryItemResponseModel> responseModels = pageOfInventoryItems
                .stream()
                .map(this::convertToResponseModel)
                .collect(Collectors.toList());
        return new MultiInventoryItemModel(responseModels, pageOfInventoryItems);
    }

    public InventoryItemResponseModel getInventoryItem(UUID inventoryId, UUID itemId) {
        InventoryEntity foundInventory = inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        inventoryAccessService.validateInventoryAccess(foundInventory);

        InventoryItemPK inventoryItemPK = new InventoryItemPK(itemId, inventoryId);
        return inventoryItemRepository.findById(inventoryItemPK)
                .map(this::convertToResponseModel)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    public void deleteInventoryItem(UUID inventoryId, UUID itemId) {
        InventoryEntity foundInventory = inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        inventoryAccessService.validateInventoryAccess(foundInventory);

        InventoryItemPK inventoryItemPK = new InventoryItemPK(itemId, inventoryId);
        InventoryItemEntity itemToDelete = inventoryItemRepository.findById(inventoryItemPK)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (itemToDelete.getQuantity() > 0) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot delete inventory item if quantity is not equal to zero");
        }
        inventoryItemRepository.deleteById(inventoryItemPK);
    }

    private InventoryItemResponseModel convertToResponseModel(InventoryItemEntity entity) {
        CatalogueItemEntity catalogueItemEntity = entity.getCatalogueItemEntity();
        return new InventoryItemResponseModel(entity.getCatalogueItemId(), entity.getInventoryId(), catalogueItemEntity.getName(), catalogueItemEntity.getPrice(), entity.getQuantity(), catalogueItemEntity.getIconId());
    }

}
