package com.usepipeline.portal.web.organization.common.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationAccountModel {
    private String organizationName;
    private String organizationAccountName;
    private Long organizationAccountId;

}
