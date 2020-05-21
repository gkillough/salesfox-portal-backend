package com.usepipeline.portal.database.organization.account.address;

import com.usepipeline.portal.database.common.AbstractAddressEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(schema = "portal", name = "organization_account_addresses")
public class OrganizationAccountAddressEntity extends AbstractAddressEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "organization_account_address_id")
    private UUID organizationAccountAddressId;

    @PrimaryKeyJoinColumn
    @Column(name = "organization_account_id")
    private UUID organizationAccountId;

}
