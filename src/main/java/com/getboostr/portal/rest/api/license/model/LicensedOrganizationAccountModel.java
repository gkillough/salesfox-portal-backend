package com.getboostr.portal.rest.api.license.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LicensedOrganizationAccountModel {
    private UUID organizationAccountId;
    private String organizationName;
    private String organizationAccountName;

}
