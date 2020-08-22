package ai.salesfox.portal.rest.api.inventory;

import ai.salesfox.portal.rest.api.inventory.model.InventoryResponseModel;
import ai.salesfox.portal.rest.api.inventory.model.MultiInventoryModel;
import ai.salesfox.portal.database.account.entity.MembershipEntity;
import ai.salesfox.portal.database.account.entity.UserEntity;
import ai.salesfox.portal.database.inventory.InventoryEntity;
import ai.salesfox.portal.database.inventory.InventoryRepository;
import ai.salesfox.portal.database.inventory.restriction.InventoryOrganizationAccountRestrictionEntity;
import ai.salesfox.portal.database.inventory.restriction.InventoryUserRestrictionEntity;
import ai.salesfox.portal.rest.api.common.page.PageRequestValidationUtils;
import ai.salesfox.portal.rest.util.HttpSafeUserMembershipRetrievalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class InventoryService {
    private final InventoryRepository inventoryRepository;
    private final InventoryAccessService inventoryAccessService;
    private final HttpSafeUserMembershipRetrievalService membershipRetrievalService;

    @Autowired
    public InventoryService(InventoryRepository inventoryRepository,
                            InventoryAccessService inventoryAccessService, HttpSafeUserMembershipRetrievalService membershipRetrievalService) {
        this.inventoryRepository = inventoryRepository;
        this.inventoryAccessService = inventoryAccessService;
        this.membershipRetrievalService = membershipRetrievalService;
    }

    public MultiInventoryModel getInventories(Integer pageOffset, Integer pageLimit) {
        PageRequestValidationUtils.validatePagingParams(pageOffset, pageLimit);
        Page<InventoryEntity> accessibleInventories = getAccessibleInventories(pageOffset, pageLimit);
        if (accessibleInventories.isEmpty()) {
            return MultiInventoryModel.empty();
        }

        List<InventoryResponseModel> inventoryModels = accessibleInventories
                .stream()
                .map(this::convertToResponseModel)
                .collect(Collectors.toList());
        return new MultiInventoryModel(inventoryModels, accessibleInventories);
    }

    public InventoryResponseModel getInventory(UUID inventoryId) {
        InventoryEntity foundInventory = inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        inventoryAccessService.validateInventoryAccess(foundInventory);
        return convertToResponseModel(foundInventory);
    }

    private Page<InventoryEntity> getAccessibleInventories(Integer pageOffset, Integer pageLimit) {
        PageRequest pageRequest = PageRequest.of(pageOffset, pageLimit);
        if (membershipRetrievalService.isAuthenticatedUserPortalAdmin()) {
            return inventoryRepository.findAll(pageRequest);
        }

        UserEntity loggedInUser = membershipRetrievalService.getAuthenticatedUserEntity();
        MembershipEntity userMembership = membershipRetrievalService.getMembershipEntity(loggedInUser);
        return inventoryRepository.findAccessibleInventories(userMembership.getOrganizationAccountId(), loggedInUser.getUserId(), pageRequest);
    }

    private InventoryResponseModel convertToResponseModel(InventoryEntity entity) {
        UUID restrictionOrgAcctId = Optional.ofNullable(entity.getInventoryOrganizationAccountRestrictionEntity())
                .map(InventoryOrganizationAccountRestrictionEntity::getOrganizationAccountId)
                .orElse(null);
        UUID restrictionUserId = Optional.ofNullable(entity.getInventoryUserRestrictionEntity())
                .map(InventoryUserRestrictionEntity::getUserId)
                .orElse(null);
        return new InventoryResponseModel(entity.getInventoryId(), restrictionOrgAcctId, restrictionUserId);
    }

}
