package com.usepipeline.portal.database.account.entity;

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
@Table(schema = "portal", name = "roles")
public class RoleEntity implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "role_id")
    private UUID roleId;

    @Column(name = "role_level")
    private String roleLevel;

    @Column(name = "role_description")
    private String description;

    @Column(name = "is_role_restricted")
    private Boolean isRoleRestricted;

}
