package ai.salesfox.portal.event;

import ai.salesfox.portal.common.enumeration.GiftTrackingStatus;
import ai.salesfox.portal.common.exception.PortalRuntimeException;
import ai.salesfox.portal.common.service.email.EmailMessagingService;
import ai.salesfox.portal.common.service.email.model.EmailMessageModel;
import ai.salesfox.portal.common.service.gift.GiftTrackingService;
import ai.salesfox.portal.database.account.entity.UserEntity;
import ai.salesfox.portal.database.account.repository.UserRepository;
import ai.salesfox.portal.database.gift.GiftEntity;
import ai.salesfox.portal.database.gift.GiftRepository;
import ai.salesfox.portal.integration.GiftPartnerSubmissionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class GiftSubmittedEventListener implements ApplicationListener<GiftSubmittedEvent> {
    private final UserRepository userRepository;
    private final GiftRepository giftRepository;
    private final GiftPartnerSubmissionService giftPartnerSubmissionService;
    private final GiftTrackingService giftTrackingService;
    private final EmailMessagingService emailMessagingService;

    @Autowired
    public GiftSubmittedEventListener(UserRepository userRepository, GiftRepository giftRepository,
                                      GiftPartnerSubmissionService giftPartnerSubmissionService, GiftTrackingService giftTrackingService, EmailMessagingService emailMessagingService) {
        this.userRepository = userRepository;
        this.giftRepository = giftRepository;
        this.giftPartnerSubmissionService = giftPartnerSubmissionService;
        this.giftTrackingService = giftTrackingService;
        this.emailMessagingService = emailMessagingService;
    }

    @Override
    public void onApplicationEvent(GiftSubmittedEvent event) {
        UserEntity submittingUser = userRepository.findById(event.getSubmittingUserId())
                .orElseThrow(() -> new PortalRuntimeException("Unable to find a submittingUser for the gift event. This is a bug."));
        GiftEntity gift = giftRepository.findById(event.getGiftId())
                .orElseThrow(() -> new PortalRuntimeException("Unable to find a gift for the gift event. This is a bug."));

        try {
            giftPartnerSubmissionService.submitGiftToPartners(gift, submittingUser);
        } catch (Exception e) {
            log.debug("There was a problem submitting the gift to Salesfox gifting partner(s)", e);
            giftTrackingService.updateGiftTrackingInfo(gift, submittingUser, GiftTrackingStatus.NOT_FULFILLABLE);
            sendEmailForException(submittingUser, gift);
        }
    }

    private void sendEmailForException(UserEntity submittingUser, GiftEntity gift) {
        try {
            String primaryMessage = String.format("Gift ID: %s <br/>" +
                    "There was a problem fulfilling the gift request.<br/>" +
                    "Please login to Salesfox for more information or contact support.", gift.getGiftId()
            );
            EmailMessageModel errorEmail = new EmailMessageModel(List.of(submittingUser.getEmail()), "[Salesfox] Distribution Failure", "Distribution Failure", primaryMessage);
            emailMessagingService.sendMessage(errorEmail);
        } catch (Exception e) {
            log.error("Failed to send email for event handling exception", e);
        }
    }

}
