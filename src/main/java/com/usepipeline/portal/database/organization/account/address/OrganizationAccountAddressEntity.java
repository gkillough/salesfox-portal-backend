package com.usepipeline.portal.database.organization.account.address;

import com.usepipeline.portal.database.common.AbstractAddressEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(schema = "portal", name = "organization_account_addresses")
public class OrganizationAccountAddressEntity extends AbstractAddressEntity {
    @Id
    @SequenceGenerator(schema = "portal", name = "org_accounts_org_account_id_seq_generator", sequenceName = "org_accounts_org_account_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "org_accounts_org_account_id_seq_generator")
    @Column(name = "organization_account_address_id")
    private Long organizationAccountAddressId;

    @PrimaryKeyJoinColumn
    @Column(name = "organization_account_id")
    private Long organizationAccountId;

}
