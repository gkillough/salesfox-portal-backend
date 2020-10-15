package ai.salesfox.portal.database.organization.account;

import ai.salesfox.portal.database.account.entity.LicenseEntity;
import ai.salesfox.portal.database.license.OrganizationAccountLicenseEntity;
import ai.salesfox.portal.database.organization.OrganizationEntity;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.util.UUID;

@Data
@NoArgsConstructor
@Entity
@Table(schema = "portal", name = "organization_accounts")
public class OrganizationAccountEntity implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "organization_account_id")
    private UUID organizationAccountId;

    @Column(name = "organization_account_name")
    private String organizationAccountName;

    @PrimaryKeyJoinColumn
    @Column(name = "license_id")
    private UUID licenseId;

    @PrimaryKeyJoinColumn
    @Column(name = "organization_id")
    private UUID organizationId;

    @Column(name = "is_active")
    private Boolean isActive;

    @OneToOne
    @JoinColumn(name = "organization_id", referencedColumnName = "organization_id", insertable = false, updatable = false)
    private OrganizationEntity organizationEntity;

    @OneToOne
    @JoinColumn(name = "license_id", referencedColumnName = "license_id", insertable = false, updatable = false)
    private LicenseEntity licenseEntity;

    @OneToOne
    @JoinColumn(name = "organization_account_id", referencedColumnName = "organization_account_id", insertable = false, updatable = false)
    private OrganizationAccountLicenseEntity organizationAccountLicenseEntity;

    public OrganizationAccountEntity(UUID organizationAccountId, String organizationAccountName, UUID licenseId, UUID organizationId, Boolean isActive) {
        this.organizationAccountId = organizationAccountId;
        this.organizationAccountName = organizationAccountName;
        this.licenseId = licenseId;
        this.organizationId = organizationId;
        this.isActive = isActive;
    }

}
