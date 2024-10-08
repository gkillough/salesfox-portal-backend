package ai.salesfox.portal.rest.api.organization.common.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationAccountSummaryModel {
    private String organizationName;
    private String organizationAccountName;
    private UUID organizationAccountId;

}
