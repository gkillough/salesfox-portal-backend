package ai.salesfox.portal.rest.api.gift.scheduling;

import ai.salesfox.portal.common.FieldValidationUtils;
import ai.salesfox.portal.common.enumeration.AccessOperation;
import ai.salesfox.portal.common.enumeration.GiftTrackingStatus;
import ai.salesfox.portal.common.model.PortalDateModel;
import ai.salesfox.portal.common.service.gift.GiftTrackingService;
import ai.salesfox.portal.database.account.entity.UserEntity;
import ai.salesfox.portal.database.gift.GiftEntity;
import ai.salesfox.portal.database.gift.GiftRepository;
import ai.salesfox.portal.database.gift.scheduling.GiftScheduleEntity;
import ai.salesfox.portal.database.gift.scheduling.GiftScheduleRepository;
import ai.salesfox.portal.rest.api.gift.util.GiftAccessService;
import ai.salesfox.portal.rest.util.HttpSafeUserMembershipRetrievalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.UUID;

@Component
public class GiftSchedulingEndpointService {
    private final GiftRepository giftRepository;
    private final GiftScheduleRepository giftScheduleRepository;
    private final GiftAccessService giftAccessService;
    private final GiftTrackingService giftTrackingService;
    private final HttpSafeUserMembershipRetrievalService membershipRetrievalService;

    @Autowired
    public GiftSchedulingEndpointService(GiftRepository giftRepository, GiftScheduleRepository giftScheduleRepository, GiftAccessService giftAccessService, GiftTrackingService giftTrackingService, HttpSafeUserMembershipRetrievalService membershipRetrievalService) {
        this.giftRepository = giftRepository;
        this.giftScheduleRepository = giftScheduleRepository;
        this.giftAccessService = giftAccessService;
        this.giftTrackingService = giftTrackingService;
        this.membershipRetrievalService = membershipRetrievalService;
    }

    @Transactional
    public GiftScheduleResponseModel scheduleGiftDraftSubmission(UUID giftId, GiftScheduleRequestModel requestModel) {
        UserEntity loggedInUser = membershipRetrievalService.getAuthenticatedUserEntity();
        GiftEntity foundGift = findGiftAndValidateAccess(giftId, loggedInUser);
        if (!foundGift.isDraft()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("Cannot schedule a gift with the current status: %s", foundGift.getGiftTrackingEntity().getStatus()));
        }
        validateRequestModel(requestModel);

        GiftScheduleEntity giftScheduleToSave = new GiftScheduleEntity(giftId, requestModel.getSendDate().toLocalDate(), loggedInUser.getUserId());
        GiftScheduleEntity savedGiftSchedule = giftScheduleRepository.save(giftScheduleToSave);

        giftTrackingService.updateGiftTrackingInfo(foundGift, loggedInUser, GiftTrackingStatus.SCHEDULED);
        return new GiftScheduleResponseModel(savedGiftSchedule.getGiftId(), savedGiftSchedule.getSendDate());
    }

    @Transactional
    public void updateGiftSchedule(UUID giftId, GiftScheduleRequestModel requestModel) {
        UserEntity loggedInUser = membershipRetrievalService.getAuthenticatedUserEntity();
        GiftEntity foundGift = findGiftAndValidateAccess(giftId, loggedInUser);
        if (!foundGift.isScheduled()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("Cannot schedule a gift with the current status: %s", foundGift.getGiftTrackingEntity().getStatus()));
        }
        validateRequestModel(requestModel);

        GiftScheduleEntity foundSchedule = giftScheduleRepository.findById(giftId)
                .orElseGet(() -> new GiftScheduleEntity(giftId, null, loggedInUser.getUserId()));
        foundSchedule.setSendDate(requestModel.getSendDate().toLocalDate());
        giftScheduleRepository.save(foundSchedule);
    }

    @Transactional
    public void unscheduleGift(UUID giftId) {
        UserEntity loggedInUser = membershipRetrievalService.getAuthenticatedUserEntity();
        GiftEntity foundGift = findGiftAndValidateAccess(giftId, loggedInUser);
        if (!foundGift.isScheduled()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("Cannot unschedule a gift with the current status: %s", foundGift.getGiftTrackingEntity().getStatus()));
        }
        giftScheduleRepository.deleteById(giftId);
        giftTrackingService.updateGiftTrackingInfo(foundGift, loggedInUser, GiftTrackingStatus.DRAFT);
    }

    private GiftEntity findGiftAndValidateAccess(UUID giftId, UserEntity loggedInUser) {
        GiftEntity foundGift = giftRepository.findById(giftId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        giftAccessService.validateGiftAccess(foundGift, loggedInUser, AccessOperation.INTERACT);
        return foundGift;
    }

    private void validateRequestModel(GiftScheduleRequestModel requestModel) {
        PortalDateModel sendDate = requestModel.getSendDate();
        if (null == sendDate) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The send date is required");
        } else if (!FieldValidationUtils.isValidDate(sendDate)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The send date is invalid");
        } else if (!sendDate.toLocalDate().isAfter(LocalDate.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The send date must be in the future");
        }
    }

}
