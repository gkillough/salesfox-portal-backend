package com.usepipeline.portal.web.license.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LicensedOrganizationAccountModel {
    private Long organizationAccountId;
    private String organizationName;
    private String organizationAccountName;

}
