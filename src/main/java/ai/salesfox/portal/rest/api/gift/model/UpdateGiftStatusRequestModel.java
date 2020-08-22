package ai.salesfox.portal.rest.api.gift.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateGiftStatusRequestModel {
    private String status;

}
