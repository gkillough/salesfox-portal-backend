package ai.salesfox.portal.rest.api.gift;

import ai.salesfox.portal.common.enumeration.AccessOperation;
import ai.salesfox.portal.common.enumeration.GiftTrackingStatus;
import ai.salesfox.portal.common.service.contact.ContactInteractionsUtility;
import ai.salesfox.portal.common.service.gift.GiftItemUtility;
import ai.salesfox.portal.common.service.gift.GiftTrackingUtility;
import ai.salesfox.portal.database.account.entity.MembershipEntity;
import ai.salesfox.portal.database.account.entity.UserEntity;
import ai.salesfox.portal.database.contact.OrganizationAccountContactRepository;
import ai.salesfox.portal.database.contact.interaction.ContactInteractionRepository;
import ai.salesfox.portal.database.gift.GiftEntity;
import ai.salesfox.portal.database.gift.GiftRepository;
import ai.salesfox.portal.database.gift.item.GiftItemDetailEntity;
import ai.salesfox.portal.database.gift.tracking.GiftTrackingDetailEntity;
import ai.salesfox.portal.database.gift.tracking.GiftTrackingDetailRepository;
import ai.salesfox.portal.database.gift.tracking.GiftTrackingEntity;
import ai.salesfox.portal.database.gift.tracking.GiftTrackingRepository;
import ai.salesfox.portal.database.inventory.InventoryRepository;
import ai.salesfox.portal.database.inventory.item.InventoryItemRepository;
import ai.salesfox.portal.rest.api.gift.model.GiftResponseModel;
import ai.salesfox.portal.rest.api.gift.model.UpdateGiftStatusRequestModel;
import ai.salesfox.portal.rest.api.gift.model.UpdateGiftTrackingDetailRequestModel;
import ai.salesfox.portal.rest.api.gift.util.EndpointGiftSubmissionUtility;
import ai.salesfox.portal.rest.api.gift.util.GiftAccessService;
import ai.salesfox.portal.rest.api.gift.util.GiftResponseModelUtils;
import ai.salesfox.portal.rest.util.HttpSafeUserMembershipRetrievalService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Component
public class GiftProcessingService {
    private final GiftRepository giftRepository;
    private final GiftTrackingDetailRepository giftTrackingDetailRepository;

    private final GiftAccessService giftAccessService;
    private final HttpSafeUserMembershipRetrievalService membershipRetrievalService;
    private final ContactInteractionsUtility contactInteractionsUtility;
    private final EndpointGiftSubmissionUtility giftSubmissionUtility;
    private final GiftTrackingUtility giftTrackingUtility;
    private final GiftItemUtility giftItemUtility;

    @Autowired
    public GiftProcessingService(GiftRepository giftRepository, GiftTrackingRepository giftTrackingRepository, GiftTrackingDetailRepository giftTrackingDetailRepository,
                                 InventoryRepository inventoryRepository, InventoryItemRepository inventoryItemRepository,
                                 GiftAccessService giftAccessService, HttpSafeUserMembershipRetrievalService membershipRetrievalService,
                                 ContactInteractionRepository contactInteractionRepository, OrganizationAccountContactRepository contactRepository) {
        this.giftRepository = giftRepository;
        this.giftTrackingDetailRepository = giftTrackingDetailRepository;
        this.giftAccessService = giftAccessService;
        this.membershipRetrievalService = membershipRetrievalService;

        this.contactInteractionsUtility = new ContactInteractionsUtility(contactRepository, contactInteractionRepository);
        this.giftTrackingUtility = new GiftTrackingUtility(giftTrackingRepository);
        this.giftItemUtility = new GiftItemUtility(inventoryRepository, inventoryItemRepository);
        this.giftSubmissionUtility = new EndpointGiftSubmissionUtility(giftTrackingUtility, giftItemUtility, contactInteractionsUtility);
    }

    @Transactional
    public GiftResponseModel submitGift(UUID giftId) {
        GiftEntity foundGift = giftRepository.findById(giftId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        UserEntity loggedInUser = membershipRetrievalService.getAuthenticatedUserEntity();
        giftAccessService.validateGiftAccess(foundGift, loggedInUser, AccessOperation.INTERACT);

        return giftSubmissionUtility.submitGift(foundGift, loggedInUser)
                .map(GiftResponseModelUtils::convertToResponseModel)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred. Please contact support if this problem persists."));
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
        giftTrackingUtility.updateGiftTrackingInfo(foundGift, loggedInUser, GiftTrackingStatus.CANCELLED.name());
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
            giftItemUtility.findInventoryItemForGift(loggedInUser, userMembership, giftItemDetail)
                    .ifPresent(giftItemUtility::incrementItemQuantity);
        }

        giftTrackingUtility.updateGiftTrackingInfo(foundGift, loggedInUser, requestModel.getStatus());
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

}
