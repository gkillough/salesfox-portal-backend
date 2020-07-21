package com.getboostr.portal.rest.organization;

import com.getboostr.portal.database.account.entity.MembershipEntity;
import com.getboostr.portal.database.account.entity.UserEntity;
import com.getboostr.portal.database.organization.OrganizationEntity;
import com.getboostr.portal.database.organization.account.OrganizationAccountEntity;
import com.getboostr.portal.rest.organization.common.model.OrganizationAccountModel;
import com.getboostr.portal.rest.util.HttpSafeUserMembershipRetrievalService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OrganizationService {
    private HttpSafeUserMembershipRetrievalService userMembershipRetrievalService;

    @Autowired
    public OrganizationService(HttpSafeUserMembershipRetrievalService userMembershipRetrievalService) {
        this.userMembershipRetrievalService = userMembershipRetrievalService;
    }

    public OrganizationAccountModel getOrganizationAccount() {
        UserEntity loggedInUserEntity = userMembershipRetrievalService.getAuthenticatedUserEntity();
        MembershipEntity membershipEntity = userMembershipRetrievalService.getMembershipEntity(loggedInUserEntity);
        OrganizationAccountEntity organizationAccountEntity = userMembershipRetrievalService.getOrganizationAccountEntity(membershipEntity);
        OrganizationEntity organizationEntity = userMembershipRetrievalService.getOrganizationEntity(organizationAccountEntity);

        return new OrganizationAccountModel(organizationEntity.getOrganizationName(), organizationAccountEntity.getOrganizationAccountName(), organizationAccountEntity.getOrganizationAccountId());
    }

}
