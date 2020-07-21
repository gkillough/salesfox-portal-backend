package com.getboostr.portal.rest.api.contact.model;

import com.getboostr.portal.common.model.PortalAddressModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContactResponseModel {
    private UUID contactId;
    private String firstName;
    private String lastName;
    private String email;
    private String mobileNumber;
    private String businessNumber;
    private PortalAddressModel address;
    private String contactOrganizationName;
    private String title;
    private Long contactInitiations;
    private Long engagementsGenerated;
    private PointOfContactUserModel pointOfContact;

}
