package ai.salesfox.portal.rest.api.contact.model;

import ai.salesfox.portal.database.contact.OrganizationAccountContactEntity;
import ai.salesfox.portal.database.contact.profile.OrganizationAccountContactProfileEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Optional;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContactSummaryModel {
    private UUID contactId;
    private String firstName;
    private String lastName;
    private String companyName;

    public static ContactSummaryModel fromEntity(OrganizationAccountContactEntity contactEntity) {
        String companyName = Optional.ofNullable(contactEntity.getContactProfileEntity()).map(OrganizationAccountContactProfileEntity::getContactOrganizationName).orElse("Company Unknown");
        return new ContactSummaryModel(contactEntity.getContactId(), contactEntity.getFirstName(), contactEntity.getLastName(), companyName);
    }

}
