package com.getboostr.portal.web.organization.profile.model;

import com.getboostr.portal.common.model.PortalAddressModel;
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
