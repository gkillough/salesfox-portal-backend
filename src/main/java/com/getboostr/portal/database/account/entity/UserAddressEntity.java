package com.getboostr.portal.database.account.entity;

import com.getboostr.portal.database.common.AbstractAddressEntity;
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
    @PrimaryKeyJoinColumn
    @Column(name = "user_id")
    private UUID userId;

}
