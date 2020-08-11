package com.getboostr.portal.rest.api.gift;

import com.getboostr.portal.common.enumeration.AccessOperation;
import com.getboostr.portal.common.enumeration.GiftTrackingStatus;
import com.getboostr.portal.common.enumeration.InteractionClassification;
import com.getboostr.portal.common.enumeration.InteractionMedium;
import com.getboostr.portal.common.service.contact.ContactInteractionsUtility;
import com.getboostr.portal.common.time.PortalDateTimeUtils;
import com.getboostr.portal.database.account.entity.MembershipEntity;
import com.getboostr.portal.database.account.entity.UserEntity;
import com.getboostr.portal.database.contact.OrganizationAccountContactRepository;
import com.getboostr.portal.database.contact.interaction.ContactInteractionRepository;
import com.getboostr.portal.database.gift.GiftEntity;
import com.getboostr.portal.database.gift.GiftRepository;
import com.getboostr.portal.database.gift.item.GiftItemDetailEntity;
import com.getboostr.portal.database.gift.tracking.GiftTrackingDetailEntity;
import com.getboostr.portal.database.gift.tracking.GiftTrackingDetailRepository;
import com.getboostr.portal.database.gift.tracking.GiftTrackingEntity;
import com.getboostr.portal.database.gift.tracking.GiftTrackingRepository;
import com.getboostr.portal.database.inventory.InventoryEntity;
import com.getboostr.portal.database.inventory.InventoryRepository;
import com.getboostr.portal.database.inventory.item.InventoryItemEntity;
import com.getboostr.portal.database.inventory.item.InventoryItemPK;
import com.getboostr.portal.database.inventory.item.InventoryItemRepository;
import com.getboostr.portal.rest.api.gift.model.GiftResponseModel;
import com.getboostr.portal.rest.api.gift.model.UpdateGiftStatusRequestModel;
import com.getboostr.portal.rest.api.gift.model.UpdateGiftTrackingDetailRequestModel;
import com.getboostr.portal.rest.api.gift.util.GiftAccessService;
import com.getboostr.portal.rest.api.gift.util.GiftResponseModelUtils;
import com.getboostr.portal.rest.util.HttpSafeUserMembershipRetrievalService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class GiftProcessingService {
    private final GiftRepository giftRepository;
    private final GiftTrackingRepository giftTrackingRepository;
    private final GiftTrackingDetailRepository giftTrackingDetailRepository;
    private final InventoryRepository inventoryRepository;
    private final InventoryItemRepository inventoryItemRepository;
    private final GiftAccessService giftAccessService;
    private final HttpSafeUserMembershipRetrievalService membershipRetrievalService;
    private final ContactInteractionsUtility<ResponseStatusException> contactInteractionsUtility;

    @Autowired
    public GiftProcessingService(GiftRepository giftRepository, GiftTrackingRepository giftTrackingRepository, GiftTrackingDetailRepository giftTrackingDetailRepository,
                                 InventoryRepository inventoryRepository, InventoryItemRepository inventoryItemRepository,
                                 GiftAccessService giftAccessService, HttpSafeUserMembershipRetrievalService membershipRetrievalService,
                                 ContactInteractionRepository contactInteractionRepository, OrganizationAccountContactRepository contactRepository) {
        this.giftRepository = giftRepository;
        this.giftTrackingRepository = giftTrackingRepository;
        this.giftTrackingDetailRepository = giftTrackingDetailRepository;
        this.inventoryRepository = inventoryRepository;
        this.inventoryItemRepository = inventoryItemRepository;
        this.giftAccessService = giftAccessService;
        this.membershipRetrievalService = membershipRetrievalService;
        this.contactInteractionsUtility = new ContactInteractionsUtility<>(membershipRetrievalService, contactRepository, contactInteractionRepository);
    }

    @Transactional
    public GiftResponseModel submitGift(UUID giftId) {
        GiftEntity foundGift = giftRepository.findById(giftId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        UserEntity loggedInUser = membershipRetrievalService.getAuthenticatedUserEntity();
        giftAccessService.validateGiftAccess(foundGift, loggedInUser, AccessOperation.INTERACT);

        if (!foundGift.isSubmittable()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This gift has already been submitted");
        }

        MembershipEntity userMembership = loggedInUser.getMembershipEntity();
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

        updateGiftTrackingInfo(foundGift, loggedInUser, GiftTrackingStatus.SUBMITTED.name());
        contactInteractionsUtility.addContactInteraction(loggedInUser, foundGift.getContactId(), InteractionMedium.MAIL, InteractionClassification.OUTGOING, "(Auto-generated) Sent gift/note");
        return GiftResponseModelUtils.convertToResponseModel(foundGift);
    }

    @Transactional
    // TODO consider changing this to "requestCancellation" in case the distributor cannot cancel the order in time
    public GiftResponseModel cancelGift(UUID giftId) {
        GiftEntity foundGift = giftRepository.findById(giftId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        UserEntity loggedInUser = membershipRetrievalService.getAuthenticatedUserEntity();
        giftAccessService.validateGiftAccess(foundGift, loggedInUser, AccessOperation.INTERACT);

        GiftTrackingEntity giftTracking = foundGift.getGiftTrackingEntity();
        if (!foundGift.isCancellable()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("This gift cannot be cancelled because its status is '%s'", giftTracking.getStatus()));
        }

        // TODO notify distributor(s)
        updateGiftTrackingInfo(foundGift, loggedInUser, GiftTrackingStatus.CANCELLED.name());
        return GiftResponseModelUtils.convertToResponseModel(foundGift);
    }

    @Transactional
    public GiftResponseModel updateGiftStatus(UUID giftId, UpdateGiftStatusRequestModel requestModel) {
        GiftEntity foundGift = giftRepository.findById(giftId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        GiftTrackingEntity foundTrackingEntity = foundGift.getGiftTrackingEntity();
        validateUpdateGiftStatusRequestModel(foundTrackingEntity.getStatus(), requestModel);

        UserEntity loggedInUser = membershipRetrievalService.getAuthenticatedUserEntity();
        MembershipEntity userMembership = loggedInUser.getMembershipEntity();

        GiftItemDetailEntity giftItemDetail = foundGift.getGiftItemDetailEntity();
        if (giftItemDetail != null && isIncompleteStatus(requestModel.getStatus())) {
            // The gift request result was incomplete, so the item must be returned to its inventory
            InventoryItemEntity inventoryItemForGift = findInventoryItemForGift(loggedInUser, userMembership, giftItemDetail);
            inventoryItemForGift.setQuantity(inventoryItemForGift.getQuantity() + 1L);
            inventoryItemRepository.save(inventoryItemForGift);
        }

        updateGiftTrackingInfo(foundGift, loggedInUser, requestModel.getStatus());
        return GiftResponseModelUtils.convertToResponseModel(foundGift);
    }

    @Transactional
    public GiftResponseModel updateGiftTrackingDetail(UUID giftId, UpdateGiftTrackingDetailRequestModel requestModel) {
        GiftEntity foundGift = giftRepository.findById(giftId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (foundGift.isSubmittable()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This gift has not been submitted");
        }
        validateUpdateGiftTrackingDetailRequestModel(requestModel);

        GiftTrackingDetailEntity trackingDetailToSave = Optional.ofNullable(foundGift.getGiftTrackingDetailEntity())
                .orElseGet(() -> new GiftTrackingDetailEntity(giftId, null, null));
        trackingDetailToSave.setDistributor(requestModel.getDistributor());
        trackingDetailToSave.setTrackingNumber(requestModel.getTrackingId());

        GiftTrackingDetailEntity savedTrackingDetail = giftTrackingDetailRepository.save(trackingDetailToSave);
        foundGift.setGiftTrackingDetailEntity(savedTrackingDetail);
        foundGift.getGiftTrackingEntity().setGiftTrackingDetailEntity(savedTrackingDetail);

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

    private void updateGiftTrackingInfo(GiftEntity gift, UserEntity updatingUser, String status) {
        GiftTrackingEntity giftTrackingToUpdate = gift.getGiftTrackingEntity();
        giftTrackingToUpdate.setStatus(status);
        giftTrackingToUpdate.setUpdatedByUserId(updatingUser.getUserId());
        giftTrackingToUpdate.setDateUpdated(PortalDateTimeUtils.getCurrentDateTimeUTC());
        GiftTrackingEntity savedGiftTracking = giftTrackingRepository.save(giftTrackingToUpdate);
        gift.setGiftTrackingEntity(savedGiftTracking);
    }

    private InventoryItemEntity findInventoryItemForGift(UserEntity loggedInUser, MembershipEntity userMembership, GiftItemDetailEntity giftItemDetail) {
        InventoryEntity inventory = inventoryRepository.findAccessibleInventories(userMembership.getOrganizationAccountId(), loggedInUser.getUserId(), PageRequest.of(0, 1))
                .stream()
                .findAny()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not find an inventory for the requesting user"));
        InventoryItemPK inventoryItemPK = new InventoryItemPK(giftItemDetail.getItemId(), inventory.getInventoryId());
        return inventoryItemRepository.findById(inventoryItemPK)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "The requested item does not exist in the inventory"));
    }

}
