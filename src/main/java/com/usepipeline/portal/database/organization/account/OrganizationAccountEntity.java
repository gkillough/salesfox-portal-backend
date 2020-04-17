package com.usepipeline.portal.database.organization.account;

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
    @SequenceGenerator(schema = "portal", name = "organization_accounts_organization_account_id_seq_generator", sequenceName = "organization_accounts_organization_account_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "organization_accounts_organization_account_id_seq_generator")
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

    @Column(name = "is_active")
    private Boolean isActive;

}
