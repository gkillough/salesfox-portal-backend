package com.usepipeline.portal.web.contact.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContactModel {
    private Long contactId;
    private String firstName;
    private String lastName;
    private String contactOrganizationName;
    private String title;
    private Long contactInitiations;
    private Long engagementsGenerated;
    private PointOfContactUserModel pointOfContact;

}
