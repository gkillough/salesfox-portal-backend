package com.usepipeline.portal.web.organization.users.model;

import com.usepipeline.portal.web.user.profile.model.UserProfileModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationMultiUsersModel {
    private List<UserProfileModel> users;

}
