package ai.salesfox.portal.event.gift;

import lombok.Getter;

import java.io.Serializable;
import java.util.UUID;

@Getter
public class GiftSubmittedEvent implements Serializable {
    private final UUID giftId;
    private final UUID submittingUserId;

    public GiftSubmittedEvent(UUID giftId, UUID submittingUserId) {
        this.giftId = giftId;
        this.submittingUserId = submittingUserId;
    }

}
