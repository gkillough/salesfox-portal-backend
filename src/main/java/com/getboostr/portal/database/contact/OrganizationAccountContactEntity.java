package com.getboostr.portal.database.contact;

import com.getboostr.portal.database.contact.restriction.ContactOrganizationAccountRestrictionEntity;
import com.getboostr.portal.database.contact.restriction.ContactUserRestrictionEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Optional;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(schema = "portal", name = "organization_account_contacts")
public class OrganizationAccountContactEntity implements Serializable, Contactable {
    @Id
    @Column(name = "contact_id")
    private UUID contactId;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "email")
    private String email;

    @Column(name = "is_active")
    private Boolean isActive;

    @OneToOne
    @JoinColumn(name = "contact_id", referencedColumnName = "contact_id", insertable = false, updatable = false)
    private ContactOrganizationAccountRestrictionEntity contactOrganizationAccountRestrictionEntity;

    @OneToOne
    @JoinColumn(name = "contact_id", referencedColumnName = "contact_id", insertable = false, updatable = false)
    private ContactUserRestrictionEntity contactUserRestrictionEntity;

    public OrganizationAccountContactEntity(UUID contactId, String firstName, String lastName, String email, Boolean isActive) {
        this.contactId = Optional.ofNullable(contactId).orElseGet(UUID::randomUUID);
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.isActive = isActive;
    }

}
