package ai.salesfox.portal.rest.api.organization;

import ai.salesfox.portal.database.account.entity.MembershipEntity;
import ai.salesfox.portal.database.account.entity.UserEntity;
import ai.salesfox.portal.database.organization.OrganizationEntity;
import ai.salesfox.portal.database.organization.account.OrganizationAccountEntity;
import ai.salesfox.portal.database.organization.account.OrganizationAccountRepository;
import ai.salesfox.portal.rest.api.common.page.PageRequestValidationUtils;
import ai.salesfox.portal.rest.api.organization.common.model.MultiOrganizationAccountModel;
import ai.salesfox.portal.rest.api.organization.common.model.OrganizationAccountSummaryModel;
import ai.salesfox.portal.rest.util.HttpSafeUserMembershipRetrievalService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class OrganizationService {
    private final OrganizationAccountRepository organizationAccountRepository;
    private final HttpSafeUserMembershipRetrievalService userMembershipRetrievalService;

    @Autowired
    public OrganizationService(OrganizationAccountRepository organizationAccountRepository, HttpSafeUserMembershipRetrievalService userMembershipRetrievalService) {
        this.organizationAccountRepository = organizationAccountRepository;
        this.userMembershipRetrievalService = userMembershipRetrievalService;
    }

    public OrganizationAccountSummaryModel getOrganizationAccount() {
        UserEntity loggedInUserEntity = userMembershipRetrievalService.getAuthenticatedUserEntity();
        MembershipEntity membershipEntity = loggedInUserEntity.getMembershipEntity();
        OrganizationAccountEntity organizationAccountEntity = membershipEntity.getOrganizationAccountEntity();
        OrganizationEntity organizationEntity = userMembershipRetrievalService.getOrganizationEntity(organizationAccountEntity);

        return new OrganizationAccountSummaryModel(organizationEntity.getOrganizationName(), organizationAccountEntity.getOrganizationAccountName(), organizationAccountEntity.getOrganizationAccountId());
    }

    public MultiOrganizationAccountModel getOrganizationAccounts(Integer pageOffset, Integer pageLimit, String query) {
        PageRequestValidationUtils.validatePagingParams(pageOffset, pageLimit);

        PageRequest pageRequest = PageRequest.of(pageOffset, pageLimit);
        Page<OrganizationAccountEntity> pageOfOrgAccounts;
        if (StringUtils.isBlank(query)) {
            pageOfOrgAccounts = organizationAccountRepository.findAll(pageRequest);
        } else {
            pageOfOrgAccounts = organizationAccountRepository.findByQuery(query, pageRequest);
        }

        if (pageOfOrgAccounts.isEmpty()) {
            return MultiOrganizationAccountModel.empty();
        }

        List<OrganizationAccountSummaryModel> orgAcctResponseModels = pageOfOrgAccounts
                .stream()
                .map(this::convertToResponseModel)
                .collect(Collectors.toList());
        return new MultiOrganizationAccountModel(orgAcctResponseModels, pageOfOrgAccounts);
    }

    private OrganizationAccountSummaryModel convertToResponseModel(OrganizationAccountEntity organizationAccount) {
        OrganizationEntity org = organizationAccount.getOrganizationEntity();
        return new OrganizationAccountSummaryModel(org.getOrganizationName(), organizationAccount.getOrganizationAccountName(), organizationAccount.getOrganizationAccountId());
    }

}
