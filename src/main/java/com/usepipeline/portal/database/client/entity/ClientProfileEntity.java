package com.usepipeline.portal.database.client.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(schema = "portal", name = "client_profiles")
public class ClientProfileEntity {
    @Id
    @SequenceGenerator(schema = "portal", name = "client_profiles_profile_id_seq_generator", sequenceName = "client_profiles_profile_id_seq", allocationSize = 100, initialValue = 100)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "client_profiles_profile_id_seq_generator")
    @Column(name = "profile_id")
    private Long profileId;

    @PrimaryKeyJoinColumn
    @Column(name = "client_id")
    private Long clientId;

    @PrimaryKeyJoinColumn
    @Column(name = "client_address_id")
    private Long clientAddressId;

    @Column(name = "organization_name")
    private String organizationName;

    @Column(name = "title")
    private String title;

    @Column(name = "organization_point_of_contact")
    private String organizationPointOfContact;

    @Column(name = "mobile_number")
    private String mobileNumber;

    @Column(name = "business_number")
    private String businessNumber;

}
