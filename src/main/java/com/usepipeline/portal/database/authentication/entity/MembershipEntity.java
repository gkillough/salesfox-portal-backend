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
@Table(schema = "portal", name = "memberships")
public class MembershipEntity implements Serializable {
    @Id
    @Column(name = "membership_id")
    private Long membershipId;

    @PrimaryKeyJoinColumn
    @Column(name = "user_id")
    private Long userId;

    @PrimaryKeyJoinColumn
    @Column(name = "organization_account_id")
    private Long organizationAccountId;

    @PrimaryKeyJoinColumn
    @Column(name = "role_id")
    private Long roleId;

    @Column(name = "is_active")
    private Boolean isActive;

}
