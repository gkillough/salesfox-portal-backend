package com.usepipeline.portal.database.authentication.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Date;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(schema = "portal", name = "licenses")
public class LicenseEntity implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "license_id")
    private Long licenseId;

    @Column(name = "license_hash")
    private UUID licenseHash;

    @Column(name = "expiration_date")
    private Date expirationDate;

    @Column(name = "type")
    private String type;

    @Column(name = "license_seats")
    private Integer licenseSeats;

    @Column(name = "monthly_cost")
    private Double monthlyCost;

    @Column(name = "is_active")
    private Boolean isActive;

}
