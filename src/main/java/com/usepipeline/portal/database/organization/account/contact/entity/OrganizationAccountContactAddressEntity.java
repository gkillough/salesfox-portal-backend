package com.usepipeline.portal.database.organization.account.contact.entity;

import com.usepipeline.portal.database.common.AbstractAddressEntity;
import com.usepipeline.portal.database.organization.account.contact.Contactable;
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
@Table(schema = "portal", name = "organization_account_contact_addresses")
public class OrganizationAccountContactAddressEntity extends AbstractAddressEntity implements Contactable {
    @Id
    @SequenceGenerator(schema = "portal", name = "org_account_contact_addresses_address_id_seq_generator", sequenceName = "org_account_contact_addresses_address_id_seq", allocationSize = 100, initialValue = 100)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "org_account_contact_addresses_address_id_seq_generator")
    @Column(name = "address_id")
    private Long addressId;

    @PrimaryKeyJoinColumn
    @Column(name = "contact_id")
    private Long contactId;

}
