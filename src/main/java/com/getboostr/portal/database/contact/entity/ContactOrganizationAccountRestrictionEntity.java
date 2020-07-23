package com.getboostr.portal.database.contact.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(schema = "portal", name = "contact_organization_account_restrictions")
public class ContactOrganizationAccountRestrictionEntity {
    @Id
    @PrimaryKeyJoinColumn
    @Column(name = "contact_id")
    private UUID contactId;

    @PrimaryKeyJoinColumn
    @Column(name = "organization_account_id")
    private UUID organizationAccountId;

}
