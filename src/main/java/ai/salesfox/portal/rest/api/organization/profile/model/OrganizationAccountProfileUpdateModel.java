package ai.salesfox.portal.rest.api.organization.profile.model;

import ai.salesfox.portal.common.model.PortalAddressModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationAccountProfileUpdateModel {
    private String organizationAccountName;
    private String businessPhoneNumber;
    private PortalAddressModel organizationAddress;

}
