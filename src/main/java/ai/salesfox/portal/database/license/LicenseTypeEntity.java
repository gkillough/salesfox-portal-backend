package ai.salesfox.portal.database.license;

import ai.salesfox.portal.event.license.type.LicenseTypeDatabaseListener;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@EntityListeners(LicenseTypeDatabaseListener.class)
@Table(schema = "portal", name = "license_types")
public class LicenseTypeEntity {
    @Id
    @PrimaryKeyJoinColumn
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "license_type_id")
    private UUID licenseTypeId;

    @Column(name = "name")
    private String name;

    @Column(name = "public")
    private Boolean isPublic;

    @Column(name = "monthly_cost")
    private BigDecimal monthlyCost;

    @Column(name = "campaigns_per_user_per_month")
    private Integer campaignsPerUserPerMonth;

    @Column(name = "contacts_per_campaign")
    private Integer contactsPerCampaign;

    @Column(name = "users_included")
    private Integer usersIncluded;

    @Column(name = "cost_per_additional_user")
    private BigDecimal costPerAdditionalUser;

}
