package com.usepipeline.portal.database.authentication.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(schema = "portal", name = "roles")
public class RoleEntity implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "role_id")
    private Long roleId;

    @Column(name = "role_level")
    private String roleLevel;

    @Column(name = "description")
    private String description;

    @Column(name = "is_role_restricted")
    private Boolean isRoleRestricted;

}
