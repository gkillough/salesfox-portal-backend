package com.usepipeline.portal.web.organization.profile.model;

import com.usepipeline.portal.common.model.PortalAddressModel;
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
