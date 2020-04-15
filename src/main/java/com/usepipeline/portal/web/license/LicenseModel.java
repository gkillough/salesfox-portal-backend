package com.usepipeline.portal.web.license;

import com.usepipeline.portal.database.account.entity.LicenseEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LicenseModel {
    private Long licenseId;
    private UUID licenseHash;
    private Date expirationDate;
    private String type;
    private Integer licenseSeats;
    private Double monthlyCost;
    private Boolean isActive;

    public static LicenseModel fromEntity(LicenseEntity licenseEntity) {
        return new LicenseModel(licenseEntity.getLicenseId(), licenseEntity.getLicenseHash(), licenseEntity.getExpirationDate(), licenseEntity.getType(), licenseEntity.getLicenseSeats(), licenseEntity.getMonthlyCost(), licenseEntity.getIsActive());
    }

}
