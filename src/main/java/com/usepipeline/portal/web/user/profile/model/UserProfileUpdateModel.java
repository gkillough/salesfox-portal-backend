package com.usepipeline.portal.web.user.profile.model;

import com.usepipeline.portal.common.model.PortalAddressModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileUpdateModel {
    private String firstName;
    private String lastName;
    private String email;
    private PortalAddressModel address;
    private String mobileNumber;
    private String businessNumber;

}
