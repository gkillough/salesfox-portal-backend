package ai.salesfox.portal.event.gift;

import ai.salesfox.portal.event.EventQueueConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
public class GiftSubmittedEventPublisher {
    private final RabbitTemplate rabbitTemplate;

    @Autowired
    public GiftSubmittedEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void fireGiftSubmittedEvent(UUID giftId, UUID submittingUserId) {
        log.debug("Submitting gift event with giftId=[{}], submittingUserId=[{}]", giftId, submittingUserId);
        rabbitTemplate.convertAndSend(EventQueueConfiguration.GIFT_SUBMITTED_QUEUE, new GiftSubmittedEvent(giftId, submittingUserId));
    }

}
