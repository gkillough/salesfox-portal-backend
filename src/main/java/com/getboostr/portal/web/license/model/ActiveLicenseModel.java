package com.getboostr.portal.web.license.model;

import com.getboostr.portal.database.account.entity.LicenseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ActiveLicenseModel extends LicenseModel {
    private LicensedOrganizationAccountModel organizationAccount;

    public static ActiveLicenseModel fromLicenseEntity(LicenseEntity licenseEntity, LicensedOrganizationAccountModel organizationAccount) {
        return new ActiveLicenseModel(licenseEntity.getLicenseId(), licenseEntity.getLicenseHash(), licenseEntity.getExpirationDate(), licenseEntity.getType(), licenseEntity.getAvailableLicenseSeats(), licenseEntity.getMaxLicenseSeats(), licenseEntity.getMonthlyCost(), organizationAccount);
    }

    public ActiveLicenseModel(UUID licenseId, UUID licenseHash, LocalDate expirationDate, String type, Long availableLicenseSeats, Long maxLicenseSeats, BigDecimal monthlyCost, LicensedOrganizationAccountModel organizationAccount) {
        super(licenseId, licenseHash, expirationDate, type, availableLicenseSeats, maxLicenseSeats, monthlyCost, true);
        this.organizationAccount = organizationAccount;
    }

}
