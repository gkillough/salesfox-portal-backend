package com.usepipeline.portal.web.registration.organization.model;

import com.usepipeline.portal.common.model.PortalAddressModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationAccountOwnerRegistrationModel {
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private PortalAddressModel userAddress;
    private String mobilePhoneNumber;
    private String businessPhoneNumber;

}
