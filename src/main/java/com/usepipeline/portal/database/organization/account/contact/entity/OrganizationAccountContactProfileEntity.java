package com.usepipeline.portal.database.organization.account.contact.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(schema = "portal", name = "organization_account_contact_profiles")
public class OrganizationAccountContactProfileEntity {
    @Id
    @SequenceGenerator(schema = "portal", name = "org_account_contact_profiles_profile_id_seq_generator", sequenceName = "org_account_contact_profiles_profile_id_seq", allocationSize = 100, initialValue = 100)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "org_account_contact_profiles_profile_id_seq_generator")
    @Column(name = "profile_id")
    private Long profileId;

    @PrimaryKeyJoinColumn
    @Column(name = "client_id")
    private Long clientId;

    @PrimaryKeyJoinColumn
    @Column(name = "client_address_id")
    private Long clientAddressId;

    @Column(name = "organization_point_of_contact_user_id")
    private Long organizationPointOfContactUserId;

    @Column(name = "organization_name")
    private String organizationName;

    @Column(name = "title")
    private String title;

    @Column(name = "mobile_number")
    private String mobileNumber;

    @Column(name = "business_number")
    private String businessNumber;

}
