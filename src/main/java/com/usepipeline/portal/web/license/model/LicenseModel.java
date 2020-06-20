package com.usepipeline.portal.web.license.model;

import com.usepipeline.portal.database.account.entity.LicenseEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LicenseModel {
    private UUID licenseId;
    private UUID licenseHash;
    private LocalDate expirationDate;
    private String type;
    private Long availableLicenseSeats;
    private Long maxLicenseSeats;
    private BigDecimal monthlyCost;
    private Boolean isActive;

    public static LicenseModel fromEntity(LicenseEntity licenseEntity) {
        return new LicenseModel(licenseEntity.getLicenseId(), licenseEntity.getLicenseHash(), licenseEntity.getExpirationDate(), licenseEntity.getType(), licenseEntity.getAvailableLicenseSeats(), licenseEntity.getMaxLicenseSeats(), licenseEntity.getMonthlyCost(), licenseEntity.getIsActive());
    }

}
