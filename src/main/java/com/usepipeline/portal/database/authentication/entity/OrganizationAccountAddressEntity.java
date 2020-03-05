package com.usepipeline.portal.database.authentication.entity;

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
@Table(schema = "portal", name = "organization_account_address_id")
public class OrganizationAccountAddressEntity extends AbstractAddressEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "organization_account_address_id")
    private Long organizationAccountAddressId;

    @PrimaryKeyJoinColumn
    @Column(name = "organization_account_id")
    private Long organizationAccountId;

}
