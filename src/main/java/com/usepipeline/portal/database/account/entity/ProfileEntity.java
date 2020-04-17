package com.usepipeline.portal.database.account.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(schema = "portal", name = "profiles")
public class ProfileEntity implements Serializable {
    @Id
    @SequenceGenerator(schema = "portal", name = "profiles_profile_id_seq_generator", sequenceName = "profiles_profile_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "profiles_profile_id_seq_generator")
    @Column(name = "profile_id")
    private Long profileId;

    @PrimaryKeyJoinColumn
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "mobile_number")
    private String mobileNumber;

    @Column(name = "business_number")
    private String businessNumber;

    @PrimaryKeyJoinColumn
    @Column(name = "mailing_address_id")
    private Long mailingAddressId;

}
