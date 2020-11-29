package ai.salesfox.portal.event;

import ai.salesfox.portal.common.service.email.EmailMessagingService;
import ai.salesfox.portal.common.service.email.PortalEmailAddressConfiguration;
import ai.salesfox.portal.common.service.email.model.EmailMessageModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class DefaultDLQHandler {
    private final EmailMessagingService emailMessagingService;
    private final PortalEmailAddressConfiguration portalEmailAddressConfiguration;

    @Autowired
    public DefaultDLQHandler(EmailMessagingService emailMessagingService, PortalEmailAddressConfiguration portalEmailAddressConfiguration) {
        this.emailMessagingService = emailMessagingService;
        this.portalEmailAddressConfiguration = portalEmailAddressConfiguration;
    }

    public void handleQueuedMessageFailure(String queueName, Message message) {
        try {
            log.error(String.format("Could not send message: %s", message.toString()));
            String primaryMessage = String.format("Queued Message Handling Failed: %s <br/>", message.toString());

            String portalSupportEmail = portalEmailAddressConfiguration.getSupportEmailAddress();
            EmailMessageModel errorEmail = new EmailMessageModel(List.of(portalSupportEmail), "[Salesfox] Event Handling Failure", "Event Handling Failure", primaryMessage);
            emailMessagingService.sendMessage(errorEmail);
        } catch (Exception e) {
            log.error("Handler failed for {}: {}", queueName, e.getMessage());
        }
    }

}
