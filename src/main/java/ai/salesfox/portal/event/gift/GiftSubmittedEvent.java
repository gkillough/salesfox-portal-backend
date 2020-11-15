package ai.salesfox.portal.event.gift;

import lombok.Getter;

import java.io.Serializable;
import java.util.UUID;

public class GiftSubmittedEvent implements Serializable {
    @Getter
    private final UUID giftId;
    @Getter
    private final UUID submittingUserId;

    public GiftSubmittedEvent(UUID giftId, UUID submittingUserId) {
        this.giftId = giftId;
        this.submittingUserId = submittingUserId;
    }

}
