package com.usepipeline.portal.web.organization.users;

import com.usepipeline.portal.web.organization.common.OrganizationEndpointConstants;
import com.usepipeline.portal.web.organization.users.model.OrganizationMultiUsersModel;
import com.usepipeline.portal.web.user.profile.model.UserProfileModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(OrganizationUsersController.BASE_ENDPOINT)
public class OrganizationUsersController {
    public static final String BASE_ENDPOINT = OrganizationEndpointConstants.BASE_ENDPOINT + "/account/{accountId}";

    @GetMapping("/users")
    public OrganizationMultiUsersModel getOrganizationAccountUsers(@PathVariable Long accountId) {
        // TODO implement
        return null;
    }

    @GetMapping("/users/{userId}")
    public UserProfileModel getOrganizationAccountUser(@PathVariable Long accountId, @PathVariable Long userId) {
        // TODO implement
        return null;
    }

    @GetMapping("/account_owner")
    public UserProfileModel getOrganizationAccountOwner(@PathVariable Long accountId) {
        // TODO implement
        return null;
    }

}
