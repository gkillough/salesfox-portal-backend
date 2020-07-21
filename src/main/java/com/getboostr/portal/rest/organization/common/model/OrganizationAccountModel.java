package com.getboostr.portal.rest.organization.common.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationAccountModel {
    private String organizationName;
    private String organizationAccountName;
    private UUID organizationAccountId;

}
