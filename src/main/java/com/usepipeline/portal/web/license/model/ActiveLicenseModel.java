package com.usepipeline.portal.web.license.model;

import com.usepipeline.portal.database.account.entity.LicenseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ActiveLicenseModel extends LicenseModel {
    private LicensedOrganizationAccountModel organizationAccount;

    public static ActiveLicenseModel fromLicenseEntity(LicenseEntity licenseEntity, LicensedOrganizationAccountModel organizationAccount) {
        return new ActiveLicenseModel(licenseEntity.getLicenseId(), licenseEntity.getLicenseHash(), licenseEntity.getExpirationDate(), licenseEntity.getType(), licenseEntity.getLicenseSeats(), licenseEntity.getMonthlyCost(), organizationAccount);
    }

    public ActiveLicenseModel(Long licenseId, UUID licenseHash, LocalDate expirationDate, String type, Long licenseSeats, Double monthlyCost, LicensedOrganizationAccountModel organizationAccount) {
        super(licenseId, licenseHash, expirationDate, type, licenseSeats, monthlyCost, true);
        this.organizationAccount = organizationAccount;
    }

}
