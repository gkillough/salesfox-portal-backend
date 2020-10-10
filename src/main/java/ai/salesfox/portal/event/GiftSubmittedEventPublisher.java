package ai.salesfox.portal.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
public class GiftSubmittedEventPublisher {
    private final ApplicationEventPublisher applicationEventPublisher;

    @Autowired
    public GiftSubmittedEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    public void fireGiftSubmittedEvent(UUID giftId, UUID submittingUserId) {
        log.debug("Submitting gift event with giftId=[{}], submittingUserId=[{}]", giftId, submittingUserId);
        applicationEventPublisher.publishEvent(new GiftSubmittedEvent(this, giftId, submittingUserId));
    }

}
