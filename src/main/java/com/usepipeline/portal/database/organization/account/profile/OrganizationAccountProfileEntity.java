package com.usepipeline.portal.database.organization.account.profile;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(schema = "portal", name = "organization_account_profiles")
public class OrganizationAccountProfileEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "profile_id")
    private Long profileId;

    @PrimaryKeyJoinColumn
    @Column(name = "organization_account_id")
    private Long organizationAccountId;

    @PrimaryKeyJoinColumn(referencedColumnName = "organization_account_address_id")
    @Column(name = "mailing_address_id")
    private Long mailingAddressId;

    @Column(name = "business_number")
    private String businessNumber;

}
