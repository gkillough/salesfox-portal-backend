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
@Table(schema = "portal", name = "organization_account_contact_profiles")
public class OrganizationAccountContactProfileEntity implements Serializable, Contactable {
    @Id
    @SequenceGenerator(schema = "portal", name = "org_account_contact_profiles_profile_id_seq_generator", sequenceName = "org_account_contact_profiles_profile_id_seq", allocationSize = 100, initialValue = 100)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "org_account_contact_profiles_profile_id_seq_generator")
    @Column(name = "profile_id")
    private Long profileId;

    @PrimaryKeyJoinColumn
    @Column(name = "contact_id")
    private Long contactId;

    @PrimaryKeyJoinColumn
    @Column(name = "contact_address_id")
    private Long contactAddressId;

    @Column(name = "organization_point_of_contact_user_id")
    private Long organizationPointOfContactUserId;

    @Column(name = "contact_organization_name")
    private String contactOrganizationName;

    @Column(name = "title")
    private String title;

    @Column(name = "mobile_number")
    private String mobileNumber;

    @Column(name = "business_number")
    private String businessNumber;

}
