package ai.salesfox.portal.database.license;

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
@Table(schema = "portal", name = "license_types")
public class LicenseTypeEntity {
    @Id
    @PrimaryKeyJoinColumn
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "license_type_id")
    private UUID licenseTypeId;

    @Column(name = "name")
    private String name;

    @Column(name = "monthly_cost")
    private BigDecimal monthlyCost;

    @Column(name = "campaigns_per_user_per_month")
    private Integer campaignsPerUserPerMonth;

    @Column(name = "contacts_per_campaign")
    private Integer contactsPerCampaign;

    @Column(name = "users_per_team")
    private Integer usersPerTeam;

}
