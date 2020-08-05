package com.getboostr.portal.rest.api.customization.icon;

import com.getboostr.portal.database.account.entity.MembershipEntity;
import com.getboostr.portal.database.account.entity.UserEntity;
import com.getboostr.portal.database.customization.icon.CustomIconEntity;
import com.getboostr.portal.database.customization.icon.restriction.CustomIconOrganizationAccountRestrictionEntity;
import com.getboostr.portal.database.customization.icon.restriction.CustomIconUserRestrictionEntity;
import com.getboostr.portal.rest.util.HttpSafeUserMembershipRetrievalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class CustomIconAccessService {
    private final HttpSafeUserMembershipRetrievalService membershipRetrievalService;

    @Autowired
    public CustomIconAccessService(HttpSafeUserMembershipRetrievalService membershipRetrievalService) {
        this.membershipRetrievalService = membershipRetrievalService;
    }

    public void validateImageAccess(CustomIconEntity customIcon) {
        if (membershipRetrievalService.isAuthenticatedUserPortalAdmin()) {
            return;
        }

        UserEntity loggedInUser = membershipRetrievalService.getAuthenticatedUserEntity();
        if (membershipRetrievalService.isAuthenticateUserBasicOrPremiumMember()) {
            CustomIconUserRestrictionEntity userRestriction = customIcon.getCustomIconUserRestrictionEntity();
            if (null != userRestriction && userRestriction.getUserId().equals(loggedInUser.getUserId())) {
                return;
            }
        } else {
            MembershipEntity userMembership = loggedInUser.getMembershipEntity();
            CustomIconOrganizationAccountRestrictionEntity orgAcctRestriction = customIcon.getCustomIconOrganizationAccountRestrictionEntity();
            if (null != orgAcctRestriction && orgAcctRestriction.getOrganizationAccountId().equals(userMembership.getOrganizationAccountId())) {
                return;
            }
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN);
    }

}
