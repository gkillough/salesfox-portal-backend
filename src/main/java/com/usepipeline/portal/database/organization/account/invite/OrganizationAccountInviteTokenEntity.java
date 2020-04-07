package com.usepipeline.portal.database.organization.account.invite;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@IdClass(OrganizationAccountInviteTokenPK.class)
@Table(schema = "portal", name = "organization_account_invite_tokens")
// TODO add table in liquibase
public class OrganizationAccountInviteTokenEntity {
    @Id
    @Column(name = "email")
    private String email;
    @Id
    @Column(name = "token")
    private String token;
    @Column(name = "organization_account_id")
    private Long organizationAccountId;
    @Column(name = "role_level")
    private String roleLevel;
    @Column(name = "date_generated")
    private LocalDateTime dateGenerated;

}
