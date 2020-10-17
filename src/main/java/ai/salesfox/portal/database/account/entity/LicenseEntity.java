package ai.salesfox.portal.database.account.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@Entity
@Table(schema = "portal", name = "licenses")
@Deprecated
public class LicenseEntity implements Serializable {
    @Id
    @PrimaryKeyJoinColumn
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "license_id")
    private UUID licenseId;

    @Column(name = "license_hash")
    private UUID licenseHash;

    @Column(name = "expiration_date")
    private LocalDate expirationDate;

    @Column(name = "type")
    private String type;

    @Column(name = "available_license_seats")
    private Long availableLicenseSeats;

    @Column(name = "max_license_seats")
    private Long maxLicenseSeats;

    @Column(name = "monthly_cost")
    private BigDecimal monthlyCost;

    @Column(name = "is_active")
    private Boolean isActive;

}
