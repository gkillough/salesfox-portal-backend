package com.usepipeline.portal.web.contact.model;

import com.usepipeline.portal.common.model.PortalAddressModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContactUploadModel {
    private String firstName;
    private String lastName;
    private String email;
    private PortalAddressModel address;
    private UUID pointOfContactUserId;
    private String contactOrganizationName;
    private String title;
    private String mobileNumber;
    private String businessNumber;

}
