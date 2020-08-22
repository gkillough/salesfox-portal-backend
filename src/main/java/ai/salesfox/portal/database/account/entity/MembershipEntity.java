package ai.salesfox.portal.database.account.entity;

import ai.salesfox.portal.database.organization.account.OrganizationAccountEntity;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.util.UUID;

@Data
@NoArgsConstructor
@Entity
@Table(schema = "portal", name = "memberships")
public class MembershipEntity implements Serializable {
    @Id
    @PrimaryKeyJoinColumn
    @Column(name = "user_id")
    private UUID userId;

    @PrimaryKeyJoinColumn
    @Column(name = "organization_account_id")
    private UUID organizationAccountId;

    @PrimaryKeyJoinColumn
    @Column(name = "role_id")
    private UUID roleId;

    @OneToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "organization_account_id", referencedColumnName = "organization_account_id", insertable = false, updatable = false)
    private OrganizationAccountEntity organizationAccountEntity;

    @OneToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", referencedColumnName = "role_id", insertable = false, updatable = false)
    private RoleEntity roleEntity;

    public MembershipEntity(UUID userId, UUID organizationAccountId, UUID roleId) {
        this.userId = userId;
        this.organizationAccountId = organizationAccountId;
        this.roleId = roleId;
    }

}
