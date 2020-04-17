package com.usepipeline.portal.database.account.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(schema = "portal", name = "licenses")
public class LicenseEntity implements Serializable {
    @Id
    @SequenceGenerator(schema = "portal", name = "licenses_license_id_seq_generator", sequenceName = "licenses_license_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "licenses_license_id_seq_generator")
    @Column(name = "license_id")
    private Long licenseId;

    @Column(name = "license_hash")
    private UUID licenseHash;

    @Column(name = "expiration_date")
    private LocalDate expirationDate;

    @Column(name = "type")
    private String type;

    @Column(name = "license_seats")
    private Long licenseSeats;

    @Column(name = "monthly_cost")
    private Double monthlyCost;

    @Column(name = "is_active")
    private Boolean isActive;

}
