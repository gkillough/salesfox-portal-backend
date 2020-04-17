package com.usepipeline.portal.database.account.entity;

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
    @SequenceGenerator(schema = "portal", name = "roles_role_id_seq_generator", sequenceName = "roles_role_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "roles_role_id_seq_generator")
    @Column(name = "role_id")
    private Long roleId;

    @Column(name = "role_level")
    private String roleLevel;

    @Column(name = "role_description")
    private String description;

    @Column(name = "is_role_restricted")
    private Boolean isRoleRestricted;

}
