package com.usepipeline.portal.database.organization.account.profile;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(schema = "portal", name = "organization_account_profiles")
public class OrganizationAccountProfileEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "profile_id")
    private UUID profileId;

    @PrimaryKeyJoinColumn
    @Column(name = "organization_account_id")
    private UUID organizationAccountId;

    @PrimaryKeyJoinColumn(referencedColumnName = "organization_account_address_id")
    @Column(name = "mailing_address_id")
    private UUID mailingAddressId;

    @Column(name = "business_number")
    private String businessNumber;

}
