package ai.salesfox.portal.rest.api.campaign;

import ai.salesfox.portal.database.account.entity.UserEntity;
import ai.salesfox.portal.database.account.repository.UserRepository;
import ai.salesfox.portal.database.campaign.UserCampaignSendDateRepository;
import ai.salesfox.portal.rest.api.campaign.model.MultiUserCampaignResponseModel;
import ai.salesfox.portal.rest.api.user.common.UserAccessService;
import ai.salesfox.portal.rest.util.HttpSafeUserMembershipRetrievalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Component
public class CampaignStatisticsEndpointService {
    private final UserRepository userRepository;
    private final UserAccessService userAccessService;
    private final UserCampaignSendDateRepository userCampaignSendDateRepository;
    private final HttpSafeUserMembershipRetrievalService membershipRetrievalService;

    @Autowired
    public CampaignStatisticsEndpointService(
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

    public MultiUserCampaignResponseModel getUserCampaigns(UUID userId, Integer pageOffset, Integer pageLimit, Integer inNumberOfDays) {
        UserEntity foundUser = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        UserEntity loggedInUser = membershipRetrievalService.getAuthenticatedUserEntity();
        // TODO update this service: userAccessService.canCurrentUserAccessDataForUser(foundUser.getUserId());

        // FIXME implement
        return MultiUserCampaignResponseModel.empty();
    }

    public Object getOrganizationAccountCampaigns(UUID orgAcctId, Integer pageOffset, Integer pageLimit, Integer inNumberOfDays) {
        // FIXME implement
        return null;
    }

}
