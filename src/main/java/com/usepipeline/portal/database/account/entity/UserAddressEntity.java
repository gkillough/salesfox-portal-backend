package com.usepipeline.portal.database.account.entity;

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
@Table(schema = "portal", name = "user_addresses")
public class UserAddressEntity extends AbstractAddressEntity {
    @Id
    @SequenceGenerator(schema = "portal", name = "user_addresses_user_address_id_seq_generator", sequenceName = "user_addresses_user_address_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_addresses_user_address_id_seq_generator")
    @Column(name = "user_address_id")
    private Long userAddressId;

    @PrimaryKeyJoinColumn
    @Column(name = "user_id")
    private Long userId;

}
