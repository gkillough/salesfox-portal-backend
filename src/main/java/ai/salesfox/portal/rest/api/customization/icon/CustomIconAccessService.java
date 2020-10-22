package ai.salesfox.portal.rest.api.customization.icon;

import ai.salesfox.portal.database.account.entity.MembershipEntity;
import ai.salesfox.portal.database.account.entity.UserEntity;
import ai.salesfox.portal.database.customization.icon.CustomIconEntity;
import ai.salesfox.portal.database.customization.icon.restriction.CustomIconOrganizationAccountRestrictionEntity;
import ai.salesfox.portal.rest.util.HttpSafeUserMembershipRetrievalService;
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
        MembershipEntity userMembership = loggedInUser.getMembershipEntity();
        CustomIconOrganizationAccountRestrictionEntity orgAcctRestriction = customIcon.getCustomIconOrganizationAccountRestrictionEntity();
        if (null != orgAcctRestriction && orgAcctRestriction.getOrganizationAccountId().equals(userMembership.getOrganizationAccountId())) {
            return;
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN);
    }

}
