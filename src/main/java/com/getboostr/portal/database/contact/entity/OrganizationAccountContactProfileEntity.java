package com.getboostr.portal.database.contact.entity;

import com.getboostr.portal.database.contact.Contactable;
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
@Table(schema = "portal", name = "organization_account_contact_profiles")
public class OrganizationAccountContactProfileEntity implements Serializable, Contactable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "profile_id")
    private UUID profileId;

    @PrimaryKeyJoinColumn
    @Column(name = "contact_id")
    private UUID contactId;

    @PrimaryKeyJoinColumn
    @Column(name = "contact_address_id")
    private UUID contactAddressId;

    @Column(name = "organization_point_of_contact_user_id")
    private UUID organizationPointOfContactUserId;

    @Column(name = "contact_organization_name")
    private String contactOrganizationName;

    @Column(name = "title")
    private String title;

    @Column(name = "mobile_number")
    private String mobileNumber;

    @Column(name = "business_number")
    private String businessNumber;

}
