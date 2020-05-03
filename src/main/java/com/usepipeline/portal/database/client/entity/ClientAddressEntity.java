package com.usepipeline.portal.database.client.entity;

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
@Table(schema = "portal", name = "client_addresses")
public class ClientAddressEntity extends AbstractAddressEntity {
    @Id
    @SequenceGenerator(schema = "portal", name = "client_addresses_client_addresses_id_seq_generator", sequenceName = "client_addresses_client_addresses_id_seq", allocationSize = 100, initialValue = 100)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "client_addresses_client_addresses_id_seq_generator")
    @Column(name = "client_address_id")
    private Long clientAddressId;

    @PrimaryKeyJoinColumn
    @Column(name = "client_id")
    private Long clientId;

}
