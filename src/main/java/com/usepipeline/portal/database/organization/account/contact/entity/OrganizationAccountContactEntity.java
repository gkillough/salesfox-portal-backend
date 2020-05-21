package com.usepipeline.portal.database.organization.account.contact.entity;

import com.usepipeline.portal.database.organization.account.contact.Contactable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(schema = "portal", name = "organization_account_contacts")
public class OrganizationAccountContactEntity implements Serializable, Contactable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "contact_id")
    private UUID contactId;

    @PrimaryKeyJoinColumn
    @Column(name = "organization_account_id")
    private UUID organizationAccountId;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "email")
    private String email;

    @Column(name = "is_active")
    private Boolean isActive;

}
