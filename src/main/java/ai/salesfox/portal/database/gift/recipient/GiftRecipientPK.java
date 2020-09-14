package ai.salesfox.portal.database.gift.recipient;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GiftRecipientPK implements Serializable {
    private UUID giftId;
    private UUID contactId;

}
