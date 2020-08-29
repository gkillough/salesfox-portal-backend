package ai.salesfox.portal.rest.api.note.credit.model;

import ai.salesfox.portal.rest.api.common.model.request.RestrictionModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NoteCreditsResponseModel {
    private UUID noteCreditId;
    private Integer availableQuantity;
    private RestrictionModel restriction;

}
