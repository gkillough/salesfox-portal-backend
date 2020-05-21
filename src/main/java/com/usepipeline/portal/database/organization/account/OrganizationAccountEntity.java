package com.usepipeline.portal.database.organization.account;

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
@Table(schema = "portal", name = "organization_accounts")
public class OrganizationAccountEntity implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "organization_account_id")
    private UUID organizationAccountId;

    @Column(name = "organization_account_name")
    private String organizationAccountName;

    @PrimaryKeyJoinColumn
    @Column(name = "license_id")
    private UUID licenseId;

    @PrimaryKeyJoinColumn
    @Column(name = "organization_id")
    private UUID organizationId;

    @Column(name = "is_active")
    private Boolean isActive;

}
