package com.usepipeline.portal.web.contact.model;

import com.usepipeline.portal.common.model.PortalAddressModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContactUpdateModel {
    private String firstName;
    private String lastName;
    private String email;
    private PortalAddressModel address;
    private Long pointOfContactUserId;
    private String contactOrganizationName;
    private String title;
    private String mobileNumber;
    private String businessNumber;

}
