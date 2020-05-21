package com.usepipeline.portal.database.account.entity;

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
@Table(schema = "portal", name = "profiles")
public class ProfileEntity implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "profile_id")
    private UUID profileId;

    @PrimaryKeyJoinColumn
    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "mobile_number")
    private String mobileNumber;

    @Column(name = "business_number")
    private String businessNumber;

    @PrimaryKeyJoinColumn
    @Column(name = "mailing_address_id")
    private UUID mailingAddressId;

}
