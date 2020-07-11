package com.usepipeline.portal.web.gift;

import com.usepipeline.portal.common.enumeration.AccessOperation;
import com.usepipeline.portal.common.enumeration.GiftTrackingStatus;
import com.usepipeline.portal.common.time.PortalDateTimeUtils;
import com.usepipeline.portal.database.account.entity.MembershipEntity;
import com.usepipeline.portal.database.account.entity.UserEntity;
import com.usepipeline.portal.database.gift.GiftEntity;
import com.usepipeline.portal.database.gift.GiftRepository;
import com.usepipeline.portal.database.gift.item.GiftItemDetailEntity;
import com.usepipeline.portal.database.gift.tracking.GiftTrackingDetailEntity;
import com.usepipeline.portal.database.gift.tracking.GiftTrackingDetailRepository;
import com.usepipeline.portal.database.gift.tracking.GiftTrackingEntity;
import com.usepipeline.portal.database.gift.tracking.GiftTrackingRepository;
import com.usepipeline.portal.database.inventory.InventoryEntity;
import com.usepipeline.portal.database.inventory.InventoryRepository;
import com.usepipeline.portal.database.inventory.item.InventoryItemEntity;
import com.usepipeline.portal.database.inventory.item.InventoryItemPK;
import com.usepipeline.portal.database.inventory.item.InventoryItemRepository;
import com.usepipeline.portal.web.contact.ContactInteractionsService;
import com.usepipeline.portal.web.gift.model.GiftResponseModel;
import com.usepipeline.portal.web.gift.model.UpdateGiftStatusRequestModel;
import com.usepipeline.portal.web.gift.model.UpdateGiftTrackingDetailRequestModel;
import com.usepipeline.portal.web.gift.util.GiftAccessService;
import com.usepipeline.portal.web.gift.util.GiftResponseModelUtils;
import com.usepipeline.portal.web.util.HttpSafeUserMembershipRetrievalService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class GiftProcessingService {
    private GiftRepository giftRepository;
    private GiftTrackingRepository giftTrackingRepository;
    private GiftTrackingDetailRepository giftTrackingDetailRepository;
    private InventoryRepository inventoryRepository;
    private InventoryItemRepository inventoryItemRepository;
    private GiftAccessService giftAccessService;
    private HttpSafeUserMembershipRetrievalService membershipRetrievalService;
    private ContactInteractionsService contactInteractionsService;

    @Autowired
    public GiftProcessingService(GiftRepository giftRepository, GiftTrackingRepository giftTrackingRepository, GiftTrackingDetailRepository giftTrackingDetailRepository,
                                 InventoryRepository inventoryRepository, InventoryItemRepository inventoryItemRepository,
                                 GiftAccessService giftAccessService, HttpSafeUserMembershipRetrievalService membershipRetrievalService, ContactInteractionsService contactInteractionsService) {
        this.giftRepository = giftRepository;
        this.giftTrackingRepository = giftTrackingRepository;
        this.giftTrackingDetailRepository = giftTrackingDetailRepository;
        this.inventoryRepository = inventoryRepository;
        this.inventoryItemRepository = inventoryItemRepository;
        this.giftAccessService = giftAccessService;
        this.membershipRetrievalService = membershipRetrievalService;
        this.contactInteractionsService = contactInteractionsService;
    }

    @Transactional
    public GiftResponseModel sendGift(UUID giftId) {
        GiftEntity foundGift = giftRepository.findById(giftId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        giftAccessService.validateGiftAccess(foundGift, AccessOperation.INTERACT);
        if (giftTrackingRepository.existsById(giftId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This gift has already been sent");
        }

        UserEntity loggedInUser = membershipRetrievalService.getAuthenticatedUserEntity();
        MembershipEntity userMembership = membershipRetrievalService.getMembershipEntity(loggedInUser);
        GiftItemDetailEntity giftItemDetail = foundGift.getGiftItemDetailEntity();
        if (giftItemDetail != null) {
            InventoryItemEntity inventoryItemForGift = findInventoryItemForGift(loggedInUser, userMembership, giftItemDetail);
            if (inventoryItemForGift.getQuantity() < 1L) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The requested item is not in stock");
            } else {
                inventoryItemForGift.setQuantity(inventoryItemForGift.getQuantity() - 1L);
                inventoryItemRepository.save(inventoryItemForGift);
            }
        }

        OffsetDateTime currentDateTime = PortalDateTimeUtils.getCurrentDateTimeUTC();
        String trackingStatus = GiftTrackingStatus.SUBMITTED.name();

        GiftTrackingEntity giftTrackingToSave = new GiftTrackingEntity(giftId, trackingStatus, loggedInUser.getUserId(), currentDateTime, currentDateTime);
        GiftTrackingEntity savedGiftTracking = giftTrackingRepository.save(giftTrackingToSave);
        foundGift.setGiftTrackingEntity(savedGiftTracking);

        contactInteractionsService.incrementContactInitiations(foundGift.getContactId());
        return GiftResponseModelUtils.convertToResponseModel(foundGift);
    }

    @Transactional
    public GiftResponseModel updateGiftStatus(UUID giftId, UpdateGiftStatusRequestModel requestModel) {
        GiftEntity foundGift = giftRepository.findById(giftId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        GiftTrackingEntity foundTrackingEntity = giftTrackingRepository.findById(giftId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "This gift has not been sent"));
        validateUpdateGiftStatusRequestModel(foundTrackingEntity.getStatus(), requestModel);

        UserEntity loggedInUser = membershipRetrievalService.getAuthenticatedUserEntity();
        MembershipEntity userMembership = membershipRetrievalService.getMembershipEntity(loggedInUser);

        GiftItemDetailEntity giftItemDetail = foundGift.getGiftItemDetailEntity();
        if (giftItemDetail != null && isIncompleteStatus(requestModel.getStatus())) {
            // The gift request result was incomplete, so the item must be returned to its inventory
            InventoryItemEntity inventoryItemForGift = findInventoryItemForGift(loggedInUser, userMembership, giftItemDetail);
            inventoryItemForGift.setQuantity(inventoryItemForGift.getQuantity() + 1L);
            inventoryItemRepository.save(inventoryItemForGift);
        }

        OffsetDateTime currentDateTime = PortalDateTimeUtils.getCurrentDateTimeUTC();

        foundTrackingEntity.setStatus(requestModel.getStatus());
        foundTrackingEntity.setUpdatedByUserId(loggedInUser.getUserId());
        foundTrackingEntity.setDateUpdated(currentDateTime);
        giftTrackingRepository.save(foundTrackingEntity);

        return GiftResponseModelUtils.convertToResponseModel(foundGift);
    }

    @Transactional
    public GiftResponseModel updateGiftTrackingDetail(UUID giftId, UpdateGiftTrackingDetailRequestModel requestModel) {
        GiftEntity foundGift = giftRepository.findById(giftId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!giftTrackingRepository.existsById(giftId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This gift has not been sent");
        }
        validateUpdateGiftTrackingDetailRequestModel(requestModel);

        GiftTrackingDetailEntity trackingDetailToSave = giftTrackingDetailRepository.findById(giftId)
                .orElseGet(() -> new GiftTrackingDetailEntity(giftId, null, null));
        trackingDetailToSave.setDistributor(requestModel.getDistributor());
        trackingDetailToSave.setTrackingNumber(requestModel.getTrackingId());

        GiftTrackingDetailEntity savedTrackingDetail = giftTrackingDetailRepository.save(trackingDetailToSave);
        foundGift.setGiftTrackingDetailEntity(savedTrackingDetail);

        return GiftResponseModelUtils.convertToResponseModel(foundGift);
    }

    private void validateUpdateGiftStatusRequestModel(String currentStatus, UpdateGiftStatusRequestModel requestModel) {
        List<String> giftTrackingStatusNames = Arrays.stream(GiftTrackingStatus.values())
                .map(GiftTrackingStatus::name)
                .collect(Collectors.toList());
        if (StringUtils.isBlank(requestModel.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The field 'Status' cannot be blank");
        } else if (!giftTrackingStatusNames.contains(requestModel.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("The provided Status [%s] is invalid. Valid values are: %s", requestModel.getStatus(), giftTrackingStatusNames.toString()));
        } else if (isUnchangeableStatus(currentStatus)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("The current Status [%s] is not allowed to be changed", currentStatus));
        } else {
            int currentStatusIndex = giftTrackingStatusNames.indexOf(currentStatus);
            int requestStatus = giftTrackingStatusNames.indexOf(requestModel.getStatus());
            if (requestStatus < currentStatusIndex) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("The provided Status [%s] comes before the current Status [%s]", requestModel.getStatus(), currentStatus));
            }
        }
    }

    private boolean isUnchangeableStatus(String giftTrackingStatus) {
        return GiftTrackingStatus.DELIVERED.name().equals(giftTrackingStatus) || isIncompleteStatus(giftTrackingStatus);
    }

    private boolean isIncompleteStatus(String giftTrackingStatus) {
        return GiftTrackingStatus.CANCELLED.name().equals(giftTrackingStatus) || GiftTrackingStatus.NOT_FULFILLABLE.name().equals(giftTrackingStatus);
    }

    private void validateUpdateGiftTrackingDetailRequestModel(UpdateGiftTrackingDetailRequestModel requestModel) {
        boolean blankDistributor = StringUtils.isBlank(requestModel.getDistributor());
        boolean blankTrackingId = StringUtils.isBlank(requestModel.getTrackingId());
        if (!blankDistributor && blankTrackingId) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "If a Distributor is provided, a Tracking ID must also be provided");
        } else if (blankDistributor && !blankTrackingId) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "If a Tracking ID is provided, a Distributor must also be provided");
        }
    }

    private InventoryItemEntity findInventoryItemForGift(UserEntity loggedInUser, MembershipEntity userMembership, GiftItemDetailEntity giftItemDetail) {
        InventoryEntity inventory = inventoryRepository.findAccessibleInventories(userMembership.getOrganizationAccountId(), loggedInUser.getUserId(), PageRequest.of(1, 1))
                .stream()
                .findAny()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR));
        InventoryItemPK inventoryItemPK = new InventoryItemPK(giftItemDetail.getItemId(), inventory.getInventoryId());
        return inventoryItemRepository.findById(inventoryItemPK)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "The requested item does not exist in the inventory"));
    }

}
