package com.usepipeline.portal.database.account.entity;

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
@Table(schema = "portal", name = "user_addresses")
public class UserAddressEntity extends AbstractAddressEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "user_address_id")
    private UUID userAddressId;

    @PrimaryKeyJoinColumn
    @Column(name = "user_id")
    private UUID userId;

}
