package ai.salesfox.portal.database.license;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.UUID;

@Data
@NoArgsConstructor
@Entity
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

    @OneToOne
    @JoinColumn(name = "license_type_id", referencedColumnName = "license_type_id", updatable = false, insertable = false)
    private LicenseTypeEntity licenseTypeEntity;

}
