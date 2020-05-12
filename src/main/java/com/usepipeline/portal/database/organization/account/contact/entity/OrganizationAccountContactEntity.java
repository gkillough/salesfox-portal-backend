package com.usepipeline.portal.database.organization.account.contact.entity;

import com.usepipeline.portal.database.organization.account.contact.Contactable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(schema = "portal", name = "organization_account_contacts")
public class OrganizationAccountContactEntity implements Serializable, Contactable {
    @Id
    @SequenceGenerator(schema = "portal", name = "org_account_contacts_contact_id_seq_generator", sequenceName = "org_account_contacts_contact_id_seq", allocationSize = 100, initialValue = 100)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "org_account_contacts_contact_id_seq_generator")
    @Column(name = "contact_id")
    private Long contactId;

    @PrimaryKeyJoinColumn
    @Column(name = "organization_account_id")
    private Long organizationAccountId;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "email")
    private String email;

    @Column(name = "is_active")
    private Boolean isActive;

}
