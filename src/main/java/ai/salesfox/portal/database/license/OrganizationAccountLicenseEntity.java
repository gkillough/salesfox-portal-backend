package ai.salesfox.portal.database.license;

import ai.salesfox.portal.database.organization.account.OrganizationAccountEntity;
import ai.salesfox.portal.event.license.OrganizationAccountLicenseDatabaseListener;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.UUID;

@Data
@NoArgsConstructor
@Entity
@EntityListeners(OrganizationAccountLicenseDatabaseListener.class)
@Table(schema = "portal", name = "org_acct_licenses")
public class OrganizationAccountLicenseEntity {
    @Id
    @PrimaryKeyJoinColumn
    @Column(name = "organization_account_id")
    private UUID organizationAccountId;

    @PrimaryKeyJoinColumn
    @Column(name = "license_type_id")
    private UUID licenseTypeId;

    @Column(name = "active_users")
    private Integer activeUsers;

    @Column(name = "billing_day_of_month")
    private Integer billingDayOfMonth;

    @Column(name = "is_active")
    private Boolean isActive;

    @OneToOne(mappedBy = "organizationAccountLicenseEntity")
    private OrganizationAccountEntity organizationAccountEntity;

    @OneToOne
    @JoinColumn(name = "license_type_id", referencedColumnName = "license_type_id", updatable = false, insertable = false)
    private LicenseTypeEntity licenseTypeEntity;

    public OrganizationAccountLicenseEntity(UUID organizationAccountId, UUID licenseTypeId, Integer activeUsers, Integer billingDayOfMonth, Boolean isActive) {
        this.organizationAccountId = organizationAccountId;
        this.licenseTypeId = licenseTypeId;
        this.activeUsers = activeUsers;
        this.billingDayOfMonth = billingDayOfMonth;
        this.isActive = isActive;
    }

}
