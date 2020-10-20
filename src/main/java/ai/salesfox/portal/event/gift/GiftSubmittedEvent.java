package ai.salesfox.portal.event.gift;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.UUID;

public class GiftSubmittedEvent extends ApplicationEvent {
    @Getter
    private final UUID giftId;
    @Getter
    private final UUID submittingUserId;

    public GiftSubmittedEvent(Object source, UUID giftId, UUID submittingUserId) {
        super(source);
        this.giftId = giftId;
        this.submittingUserId = submittingUserId;
    }

}
