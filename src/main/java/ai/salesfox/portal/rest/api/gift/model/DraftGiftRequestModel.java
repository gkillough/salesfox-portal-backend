package ai.salesfox.portal.rest.api.gift.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DraftGiftRequestModel {
    private UUID noteId;
    private UUID itemId;
    private UUID customTextId;
    private UUID customIconId;

}
