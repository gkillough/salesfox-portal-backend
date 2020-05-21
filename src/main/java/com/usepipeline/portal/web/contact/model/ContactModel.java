package com.usepipeline.portal.web.contact.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContactModel {
    private UUID contactId;
    private String firstName;
    private String lastName;
    private String contactOrganizationName;
    private String title;
    private Long contactInitiations;
    private Long engagementsGenerated;
    private PointOfContactUserModel pointOfContact;

}
