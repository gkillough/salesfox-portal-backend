package ai.salesfox.portal.rest.api.common.model.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActiveStatusPatchModel {
    private Boolean activeStatus;

}
