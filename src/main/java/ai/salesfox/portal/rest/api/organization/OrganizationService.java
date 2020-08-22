package ai.salesfox.portal.rest.api.organization;

import ai.salesfox.portal.rest.api.organization.common.model.OrganizationAccountModel;
import ai.salesfox.portal.database.account.entity.MembershipEntity;
import ai.salesfox.portal.database.account.entity.UserEntity;
import ai.salesfox.portal.database.organization.OrganizationEntity;
import ai.salesfox.portal.database.organization.account.OrganizationAccountEntity;
import ai.salesfox.portal.rest.util.HttpSafeUserMembershipRetrievalService;
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
