package com.usepipeline.portal.database.authentication.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(schema = "portal", name = "organization_accounts")
public class OrganizationAccountEntity implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "organization_account_id")
    private Long organizationAccountId;

    @Column(name = "organization_account_name")
    private String organizationAccountName;

    @PrimaryKeyJoinColumn
    @Column(name = "license_id")
    private Long licenseId;

    @PrimaryKeyJoinColumn
    @Column(name = "organization_id")
    private Long organizationId;

    @PrimaryKeyJoinColumn
    @Column(name = "organization_account_address_id")
    private Long organizationAccountAddressId;

    @Column(name = "is_active")
    private Boolean isActive;

}
