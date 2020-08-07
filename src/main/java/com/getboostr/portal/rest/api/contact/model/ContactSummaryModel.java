package com.getboostr.portal.rest.api.contact.model;

import com.getboostr.portal.database.contact.OrganizationAccountContactEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContactSummaryModel {
    private UUID contactId;
    private String firstName;
    private String lastName;

    public static ContactSummaryModel fromEntity(OrganizationAccountContactEntity contactEntity) {
        return new ContactSummaryModel(contactEntity.getContactId(), contactEntity.getFirstName(), contactEntity.getLastName());
    }

}
