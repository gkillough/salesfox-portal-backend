package ai.salesfox.portal.rest.api.campaign;

import ai.salesfox.portal.common.time.PortalDateTimeUtils;
import ai.salesfox.portal.database.account.entity.UserEntity;
import ai.salesfox.portal.database.account.repository.UserRepository;
import ai.salesfox.portal.database.campaign.UserCampaignSendDateEntity;
import ai.salesfox.portal.database.campaign.UserCampaignSendDateRepository;
import ai.salesfox.portal.rest.api.campaign.model.CampaignSummaryResponseModel;
import ai.salesfox.portal.rest.api.campaign.model.MultiCampaignSummaryResponseModel;
import ai.salesfox.portal.rest.api.user.common.UserAccessService;
import ai.salesfox.portal.rest.api.user.common.model.UserSummaryModel;
import ai.salesfox.portal.rest.util.HttpSafeUserMembershipRetrievalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
    private final UserCampaignSendDateRepository userCampaignSendDateRepository;
    private final HttpSafeUserMembershipRetrievalService membershipRetrievalService;

    @Autowired
    public CampaignSummaryEndpointService(
            UserRepository userRepository,
            UserAccessService userAccessService,
            UserCampaignSendDateRepository userCampaignSendDateRepository,
            HttpSafeUserMembershipRetrievalService membershipRetrievalService
    ) {
        this.userRepository = userRepository;
        this.userAccessService = userAccessService;
        this.userCampaignSendDateRepository = userCampaignSendDateRepository;
        this.membershipRetrievalService = membershipRetrievalService;
    }

    public MultiCampaignSummaryResponseModel getUserCampaignSummaries(UUID userId, Integer pageOffset, Integer pageLimit, Integer inNumberOfDays) {
        UserEntity foundUser = findAndValidateUser(userId);
        Page<UserCampaignSendDateEntity> pageOfCampaignsForUser = getPageOfCampaignsForUser(userId, pageOffset, pageLimit, inNumberOfDays);

        UserSummaryModel foundUserSummary = UserSummaryModel.fromEntity(foundUser);
        if (pageOfCampaignsForUser.isEmpty()) {
            return MultiCampaignSummaryResponseModel.empty(foundUserSummary);
        }

        List<CampaignSummaryResponseModel> campaignSummaries = pageOfCampaignsForUser
                .stream()
                .map(this::convertToUserCampaignResponseModel)
                .collect(Collectors.toList());
        return new MultiCampaignSummaryResponseModel(foundUserSummary, campaignSummaries, pageOfCampaignsForUser);
    }

    public Object getOrganizationAccountCampaigns(UUID orgAcctId, Integer pageOffset, Integer pageLimit, Integer inNumberOfDays) {
        // FIXME implement
        return null;
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

    private Page<UserCampaignSendDateEntity> getPageOfCampaignsForUser(UUID userId, Integer pageOffset, Integer pageLimit, Integer inNumberOfDays) {
        LocalDate today = PortalDateTimeUtils.getCurrentDate();
        LocalDate queryStartDate = today.minusDays(inNumberOfDays);

        // TODO consider sorting
        PageRequest pageRequest = PageRequest.of(pageOffset, pageLimit);
        return userCampaignSendDateRepository.findByUserIdOnOrAfter(userId, queryStartDate, pageRequest);
    }

    private CampaignSummaryResponseModel convertToUserCampaignResponseModel(UserCampaignSendDateEntity userCampaign) {
        return new CampaignSummaryResponseModel(userCampaign.getDate(), userCampaign.getRecipientCount());
    }

}
