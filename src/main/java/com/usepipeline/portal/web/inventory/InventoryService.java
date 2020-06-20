package com.usepipeline.portal.web.inventory;

import com.usepipeline.portal.database.account.entity.MembershipEntity;
import com.usepipeline.portal.database.account.entity.UserEntity;
import com.usepipeline.portal.database.inventory.InventoryEntity;
import com.usepipeline.portal.database.inventory.InventoryRepository;
import com.usepipeline.portal.database.inventory.item.InventoryItemRepository;
import com.usepipeline.portal.web.common.page.PageRequestValidationUtils;
import com.usepipeline.portal.web.inventory.model.InventoryRequestModel;
import com.usepipeline.portal.web.inventory.model.InventoryResponseModel;
import com.usepipeline.portal.web.inventory.model.MultiInventoryModel;
import com.usepipeline.portal.web.security.authorization.PortalAuthorityConstants;
import com.usepipeline.portal.web.util.HttpSafeUserMembershipRetrievalService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class InventoryService {
    private InventoryRepository inventoryRepository;
    private InventoryItemRepository inventoryItemRepository;
    private InventoryAccessService inventoryAccessService;
    private HttpSafeUserMembershipRetrievalService membershipRetrievalService;

    @Autowired
    public InventoryService(InventoryRepository inventoryRepository, InventoryItemRepository inventoryItemRepository,
                            InventoryAccessService inventoryAccessService, HttpSafeUserMembershipRetrievalService membershipRetrievalService) {
        this.inventoryRepository = inventoryRepository;
        this.inventoryItemRepository = inventoryItemRepository;
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

    @Transactional
    public InventoryResponseModel createInventory(InventoryRequestModel requestModel) {
        validateInventoryRequestModel(requestModel);
        InventoryEntity inventoryToSave = new InventoryEntity();
        inventoryToSave.setInventoryName(requestModel.getInventoryName());

        UserEntity loggedInUser = membershipRetrievalService.getAuthenticatedUserEntity();
        MembershipEntity userMembership = membershipRetrievalService.getMembershipEntity(loggedInUser);
        inventoryToSave.setOrganizationAccountId(userMembership.getOrganizationAccountId());

        String roleLevel = membershipRetrievalService.getRoleEntity(userMembership).getRoleLevel();
        if (PortalAuthorityConstants.PIPELINE_BASIC_USER.equals(roleLevel) || PortalAuthorityConstants.PIPELINE_PREMIUM_USER.equals(roleLevel)) {
            inventoryToSave.setUserId(loggedInUser.getUserId());
        }

        InventoryEntity savedInventory = inventoryRepository.save(inventoryToSave);
        return convertToResponseModel(savedInventory);
    }

    @Transactional
    public void updateInventory(UUID inventoryId, InventoryRequestModel requestModel) {
        InventoryEntity foundInventory = inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        inventoryAccessService.validateInventoryAccess(foundInventory);
        validateInventoryRequestModel(requestModel);

        foundInventory.setInventoryName(requestModel.getInventoryName());

        InventoryEntity savedInventory = inventoryRepository.save(foundInventory);
        convertToResponseModel(savedInventory);
    }

    @Transactional
    public void deleteInventory(UUID inventoryId) {
        InventoryEntity foundInventory = inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        inventoryAccessService.validateInventoryAccess(foundInventory);

        Long numberOfInventoryItemsWithNonZeroQuantity = inventoryItemRepository.countItemsWithNonZeroQuantity(inventoryId);
        if (numberOfInventoryItemsWithNonZeroQuantity > 0) {
            throw new ResponseStatusException(HttpStatus.PRECONDITION_FAILED, "Cannot delete an inventory while items quantities are non-zero");
        }
        inventoryRepository.deleteById(inventoryId);
    }

    private Page<InventoryEntity> getAccessibleInventories(Integer pageOffset, Integer pageLimit) {
        PageRequest pageRequest = PageRequest.of(pageOffset, pageLimit);
        if (membershipRetrievalService.isAuthenticatedUserPipelineAdmin()) {
            return inventoryRepository.findAll(pageRequest);
        }
        UserEntity loggedInUser = membershipRetrievalService.getAuthenticatedUserEntity();
        MembershipEntity userMembership = membershipRetrievalService.getMembershipEntity(loggedInUser);
        return inventoryRepository.findAccessibleInventoryItems(userMembership.getOrganizationAccountId(), loggedInUser.getUserId(), pageRequest);
    }

    private void validateInventoryRequestModel(InventoryRequestModel requestModel) {
        if (StringUtils.isBlank(requestModel.getInventoryName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The field 'Inventory Name' cannot be blank");
        }
    }

    private InventoryResponseModel convertToResponseModel(InventoryEntity entity) {
        return new InventoryResponseModel(entity.getInventoryId(), entity.getInventoryName(), entity.getOrganizationAccountId(), entity.getUserId());
    }

}
