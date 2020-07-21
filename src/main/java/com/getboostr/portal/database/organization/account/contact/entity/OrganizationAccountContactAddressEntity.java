package com.getboostr.portal.database.organization.account.contact.entity;

import com.getboostr.portal.database.common.AbstractAddressEntity;
import com.getboostr.portal.database.organization.account.contact.Contactable;
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
@Table(schema = "portal", name = "organization_account_contact_addresses")
public class OrganizationAccountContactAddressEntity extends AbstractAddressEntity implements Contactable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "address_id")
    private UUID addressId;

    @PrimaryKeyJoinColumn
    @Column(name = "contact_id")
    private UUID contactId;

}
