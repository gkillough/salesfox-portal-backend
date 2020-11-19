package ai.salesfox.portal.event.gift;

import ai.salesfox.integration.common.exception.SalesfoxException;
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
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class GiftSubmittedEventListener {
    private final UserRepository userRepository;
    private final GiftRepository giftRepository;
    private final GiftPartnerSubmissionService giftPartnerSubmissionService;
    private final GiftTrackingService giftTrackingService;
    private final EmailMessagingService emailMessagingService;

    public GiftSubmittedEventListener(
            UserRepository userRepository,
            GiftRepository giftRepository,
            GiftPartnerSubmissionService giftPartnerSubmissionService,
            GiftTrackingService giftTrackingService,
            EmailMessagingService emailMessagingService
    ) {
        this.userRepository = userRepository;
        this.giftRepository = giftRepository;
        this.giftPartnerSubmissionService = giftPartnerSubmissionService;
        this.giftTrackingService = giftTrackingService;
        this.emailMessagingService = emailMessagingService;
    }

    @RabbitListener(queues = GiftSubmittedEventQueueConfiguration.GIFT_SUBMITTED_QUEUE)
    public void onGiftSubmitted(GiftSubmittedEvent event) {
        UserEntity submittingUser = userRepository.findById(event.getSubmittingUserId())
                .orElseThrow(() -> new PortalRuntimeException("Unable to find a submittingUser for the gift event. This is a bug."));
        GiftEntity gift = giftRepository.findById(event.getGiftId())
                .orElseThrow(() -> new PortalRuntimeException("Unable to find a gift for the gift event. This is a bug."));

        try {
            giftPartnerSubmissionService.submitGiftToPartners(gift, submittingUser);
        } catch (SalesfoxException salesfoxException) {
            log.debug("There was a problem submitting the gift to Salesfox gifting partner(s)", salesfoxException);
            giftTrackingService.updateGiftTrackingInfo(gift, submittingUser, GiftTrackingStatus.NOT_FULFILLABLE);
            sendEmailForException(submittingUser, gift, salesfoxException.getMessage());
        }
    }

    @RabbitListener(queues = GiftSubmittedEventQueueConfiguration.GIFT_SUBMITTED_DLQ)
    public void onGiftSubmittedProcessingFailure(Message message) {
        try {
            log.error(String.format("Could not send message: %s", message.toString()));
            String primaryMessage = String.format("Queued Message Handling Failed: %s <br/>", message.toString());

            // FIXME add real failure email address
            EmailMessageModel errorEmail = new EmailMessageModel(List.of("noreply@salesfox.ai"), "[Salesfox] Distribution Failure", "Distribution Failure", primaryMessage);
            emailMessagingService.sendMessage(errorEmail);
        } catch (Exception e) {
            log.error("Handler failed for {}: {}", GiftSubmittedEventQueueConfiguration.GIFT_SUBMITTED_DLQ, e.getMessage());
        }
    }

    private void sendEmailForException(UserEntity submittingUser, GiftEntity gift, String exceptionMessage) {
        try {
            String primaryMessage = String.format("Gift ID: %s <br/>" +
                    "There was a problem fulfilling the gift request.<br/>" +
                    "Please login to Salesfox for more information or contact support.<br/>" +
                    "Error message: %s", gift.getGiftId(), exceptionMessage
            );
            EmailMessageModel errorEmail = new EmailMessageModel(List.of(submittingUser.getEmail()), "[Salesfox] Distribution Failure", "Distribution Failure", primaryMessage);
            emailMessagingService.sendMessage(errorEmail);
        } catch (Exception e) {
            log.error("Failed to send email for event handling exception", e);
        }
    }

}
