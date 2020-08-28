package ai.salesfox.portal.rest.api.note.credit;

import ai.salesfox.portal.database.account.entity.MembershipEntity;
import ai.salesfox.portal.database.account.entity.UserEntity;
import ai.salesfox.portal.database.note.credit.NoteCreditEntity;
import ai.salesfox.portal.database.note.credit.NoteCreditOrgAccountRestrictionEntity;
import ai.salesfox.portal.database.note.credit.NoteCreditRepository;
import ai.salesfox.portal.database.note.credit.NoteCreditUserRestrictionEntity;
import ai.salesfox.portal.rest.api.common.model.request.RestrictionModel;
import ai.salesfox.portal.rest.api.note.credit.model.NoteCreditResponseModel;
import ai.salesfox.portal.rest.util.HttpSafeUserMembershipRetrievalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;

@Component
public class NoteCreditService {
    private final NoteCreditRepository noteCreditRepository;
    private final HttpSafeUserMembershipRetrievalService membershipRetrievalService;

    @Autowired
    public NoteCreditService(NoteCreditRepository noteCreditRepository, HttpSafeUserMembershipRetrievalService membershipRetrievalService) {
        this.noteCreditRepository = noteCreditRepository;
        this.membershipRetrievalService = membershipRetrievalService;
    }

    public NoteCreditResponseModel getCredits() {
        UserEntity loggedInUser = membershipRetrievalService.getAuthenticatedUserEntity();
        MembershipEntity userMembership = loggedInUser.getMembershipEntity();
        NoteCreditEntity foundNoteCredits = noteCreditRepository.findAccessibleNoteCredits(userMembership.getOrganizationAccountId(), loggedInUser.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "No accessible note credits. Please contact support."));
        // TODO initialize this entity if none exists

        UUID orgRestrictionId = Optional.ofNullable(foundNoteCredits.getNoteCreditOrgAccountRestrictionEntity())
                .map(NoteCreditOrgAccountRestrictionEntity::getOrganizationAccountId)
                .orElse(null);
        UUID userRestrictionId = Optional.ofNullable(foundNoteCredits.getNoteCreditUserRestrictionEntity())
                .map(NoteCreditUserRestrictionEntity::getUserId)
                .orElse(null);
        RestrictionModel restrictionModel = new RestrictionModel(orgRestrictionId, userRestrictionId);
        return new NoteCreditResponseModel(foundNoteCredits.getNoteCreditId(), foundNoteCredits.getAvailableCredits(), restrictionModel);
    }

}
