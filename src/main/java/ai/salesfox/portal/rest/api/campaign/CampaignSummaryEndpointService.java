package ai.salesfox.portal.rest.api.campaign;

import ai.salesfox.portal.common.enumeration.AccessOperation;
import ai.salesfox.portal.common.time.PortalDateTimeUtils;
import ai.salesfox.portal.database.account.entity.UserEntity;
import ai.salesfox.portal.database.account.repository.UserRepository;
import ai.salesfox.portal.database.campaign.UserCampaignSummaryEntity;
import ai.salesfox.portal.database.campaign.UserCampaignSummaryRepository;
import ai.salesfox.portal.database.organization.account.OrganizationAccountEntity;
import ai.salesfox.portal.database.organization.account.OrganizationAccountRepository;
import ai.salesfox.portal.rest.api.campaign.organization.model.MultiOrganizationAccountCampaignSummaryResponseModel;
import ai.salesfox.portal.rest.api.campaign.organization.model.OrganizationAccountCampaignSummaryResponseModel;
import ai.salesfox.portal.rest.api.campaign.user.model.MultiUserCampaignSummaryResponseModel;
import ai.salesfox.portal.rest.api.campaign.user.model.UserCampaignSummaryResponseModel;
import ai.salesfox.portal.rest.api.common.page.PageRequestValidationUtils;
import ai.salesfox.portal.rest.api.organization.common.OrganizationAccessService;
import ai.salesfox.portal.rest.api.user.common.UserAccessService;
import ai.salesfox.portal.rest.api.user.common.model.UserSummaryModel;
import ai.salesfox.portal.rest.util.HttpSafeUserMembershipRetrievalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class CampaignSummaryEndpointService {
    private final UserRepository userRepository;
    private final UserAccessService userAccessService;
    private final OrganizationAccountRepository organizationAccountRepository;
    private final OrganizationAccessService organizationAccessService;
    private final UserCampaignSummaryRepository userCampaignSummaryRepository;
    private final HttpSafeUserMembershipRetrievalService membershipRetrievalService;

    @Autowired
    public CampaignSummaryEndpointService(
            UserRepository userRepository,
            UserAccessService userAccessService,
            OrganizationAccountRepository organizationAccountRepository,
            OrganizationAccessService organizationAccessService,
            UserCampaignSummaryRepository userCampaignSummaryRepository,
            HttpSafeUserMembershipRetrievalService membershipRetrievalService
    ) {
        this.userRepository = userRepository;
        this.userAccessService = userAccessService;
        this.organizationAccountRepository = organizationAccountRepository;
        this.organizationAccessService = organizationAccessService;
        this.userCampaignSummaryRepository = userCampaignSummaryRepository;
        this.membershipRetrievalService = membershipRetrievalService;
    }

    public MultiUserCampaignSummaryResponseModel getUserCampaignSummaries(UUID userId, Integer pageOffset, Integer pageLimit, Integer lookbackDays) {
        PageRequestValidationUtils.validatePagingParams(pageOffset, pageLimit);
        validateLookbackDays(lookbackDays);

        UserEntity foundUser = findAndValidateUser(userId);
        Page<UserCampaignSummaryEntity> pageOfCampaignsForUser = getPageOfCampaignsForUser(userId, pageOffset, pageLimit, lookbackDays);

        UserSummaryModel foundUserSummary = UserSummaryModel.fromEntity(foundUser);
        if (pageOfCampaignsForUser.isEmpty()) {
            return MultiUserCampaignSummaryResponseModel.empty(foundUserSummary);
        }

        List<UserCampaignSummaryResponseModel> campaignSummaries = pageOfCampaignsForUser
                .stream()
                .map(this::convertToUserCampaignResponseModel)
                .collect(Collectors.toList());
        return new MultiUserCampaignSummaryResponseModel(foundUserSummary, campaignSummaries, pageOfCampaignsForUser);
    }

    public MultiOrganizationAccountCampaignSummaryResponseModel getOrganizationAccountCampaigns(UUID orgAcctId, Integer lookbackDays) {
        validateLookbackDays(lookbackDays);
        OrganizationAccountEntity foundOrgAcct = findAndValidateOrganizationAccount(orgAcctId);

        LocalDate startDate = PortalDateTimeUtils.getCurrentDate().minusDays(lookbackDays);
        List<OrganizationAccountCampaignSummaryResponseModel> responseModels =
                userCampaignSummaryRepository.summarizeForOrgAccountIdByDateOnOrAfter(foundOrgAcct.getOrganizationAccountId(), startDate)
                        .stream()
                        .map(OrganizationAccountCampaignSummaryResponseModel::fromView)
                        .collect(Collectors.toList());
        return new MultiOrganizationAccountCampaignSummaryResponseModel(responseModels);
    }

    private UserEntity findAndValidateUser(UUID userId) {
        UserEntity foundUser = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        UserEntity loggedInUser = membershipRetrievalService.getAuthenticatedUserEntity();
        if (!userAccessService.canUserAccessDataForUser(loggedInUser, foundUser)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have access to this user");
        }
        return foundUser;
    }

    private OrganizationAccountEntity findAndValidateOrganizationAccount(UUID orgAcctId) {
        OrganizationAccountEntity foundOrgAcct = organizationAccountRepository.findById(orgAcctId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        UserEntity authenticatedUser = membershipRetrievalService.getAuthenticatedUserEntity();
        if (!organizationAccessService.canUserAccessOrganizationAccount(authenticatedUser, foundOrgAcct, AccessOperation.READ)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not allowed to access this organization account");
        }
        return foundOrgAcct;
    }

    private void validateLookbackDays(Integer lookbackDays) {
        if (null == lookbackDays) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The parameter 'lookbackDays' is required");
        }
        if (lookbackDays < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The parameter 'lookbackDays' must be a positive integer");
        }
    }

    private Page<UserCampaignSummaryEntity> getPageOfCampaignsForUser(UUID userId, Integer pageOffset, Integer pageLimit, Integer lookbackDays) {
        LocalDate today = PortalDateTimeUtils.getCurrentDate();
        LocalDate queryStartDate = today.minusDays(lookbackDays);

        Sort descDateSort = Sort.by(Sort.Direction.DESC, "date");
        PageRequest pageRequest = PageRequest.of(pageOffset, pageLimit, descDateSort);
        return userCampaignSummaryRepository.findByUserIdOnOrAfter(userId, queryStartDate, pageRequest);
    }

    private UserCampaignSummaryResponseModel convertToUserCampaignResponseModel(UserCampaignSummaryEntity userCampaign) {
        return new UserCampaignSummaryResponseModel(userCampaign.getDate(), userCampaign.getRecipientCount());
    }

}
