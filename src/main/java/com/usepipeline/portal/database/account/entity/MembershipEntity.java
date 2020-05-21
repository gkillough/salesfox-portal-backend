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
@Table(schema = "portal", name = "memberships")
public class MembershipEntity implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "membership_id")
    private UUID membershipId;

    @PrimaryKeyJoinColumn
    @Column(name = "user_id")
    private UUID userId;

    @PrimaryKeyJoinColumn
    @Column(name = "organization_account_id")
    private UUID organizationAccountId;

    @PrimaryKeyJoinColumn
    @Column(name = "role_id")
    private UUID roleId;

}
