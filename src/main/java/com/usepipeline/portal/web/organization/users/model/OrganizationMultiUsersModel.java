package com.usepipeline.portal.web.organization.users.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationMultiUsersModel {
    private List<OrganizationUserAdminViewModel> users;

}
