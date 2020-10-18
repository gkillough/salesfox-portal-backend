package ai.salesfox.portal.rest.api.common.model.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RestrictionModel {
    private UUID organizationAccountId;
    // TODO remove
    @Deprecated
    private UUID userId;

}
