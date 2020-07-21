package com.getboostr.portal.web.inventory;

import com.getboostr.portal.database.account.entity.MembershipEntity;
import com.getboostr.portal.database.account.entity.UserEntity;
import com.getboostr.portal.database.inventory.InventoryEntity;
import com.getboostr.portal.database.inventory.InventoryRepository;
import com.getboostr.portal.web.inventory.model.InventoryResponseModel;
import com.getboostr.portal.web.inventory.model.MultiInventoryModel;
import com.getboostr.portal.web.common.page.PageRequestValidationUtils;
import com.getboostr.portal.web.util.HttpSafeUserMembershipRetrievalService;
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
public class InventoryService {
    private InventoryRepository inventoryRepository;
    private InventoryAccessService inventoryAccessService;
    private HttpSafeUserMembershipRetrievalService membershipRetrievalService;

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
        if (membershipRetrievalService.isAuthenticatedUserPipelineAdmin()) {
            return inventoryRepository.findAll(pageRequest);
        }

        UserEntity loggedInUser = membershipRetrievalService.getAuthenticatedUserEntity();
        MembershipEntity userMembership = membershipRetrievalService.getMembershipEntity(loggedInUser);
        return inventoryRepository.findAccessibleInventories(userMembership.getOrganizationAccountId(), loggedInUser.getUserId(), pageRequest);
    }

    private InventoryResponseModel convertToResponseModel(InventoryEntity entity) {
        return new InventoryResponseModel(entity.getInventoryId(), entity.getOrganizationAccountId(), entity.getUserId());
    }

}
