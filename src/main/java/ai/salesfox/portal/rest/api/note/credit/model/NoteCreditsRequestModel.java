package ai.salesfox.portal.rest.api.note.credit.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NoteCreditsRequestModel {
    private Integer quantity;
    private String stripeChargeToken;

}
