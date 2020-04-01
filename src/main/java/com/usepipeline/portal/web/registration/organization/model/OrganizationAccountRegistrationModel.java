package com.usepipeline.portal.web.registration.organization.model;

import com.usepipeline.portal.common.model.PortalAddressModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationAccountRegistrationModel {
    private String organizationName;
    private String organizationAccountName;
    private UUID licenseHash;
    private String businessPhoneNumber;
    private PortalAddressModel organizationAddress;
    private OrganizationAccountManagerRegistrationModel accountOwner;

}
