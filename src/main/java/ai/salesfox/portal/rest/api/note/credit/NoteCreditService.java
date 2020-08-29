package ai.salesfox.portal.rest.api.note.credit;

import ai.salesfox.portal.database.account.entity.MembershipEntity;
import ai.salesfox.portal.database.account.entity.UserEntity;
import ai.salesfox.portal.database.note.credit.*;
import ai.salesfox.portal.rest.api.common.model.request.RestrictionModel;
import ai.salesfox.portal.rest.api.note.credit.model.NoteCreditResponseModel;
import ai.salesfox.portal.rest.util.HttpSafeUserMembershipRetrievalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class NoteCreditService {
    private final NoteCreditRepository noteCreditRepository;
    private final NoteCreditUserRestrictionRepository noteCreditUserRestrictionRepository;
    private final NoteCreditOrgAccountRestrictionRepository noteCreditOrgAccountRestrictionRepository;
    private final HttpSafeUserMembershipRetrievalService membershipRetrievalService;

    @Autowired
    public NoteCreditService(NoteCreditRepository noteCreditRepository, NoteCreditUserRestrictionRepository noteCreditUserRestrictionRepository, NoteCreditOrgAccountRestrictionRepository noteCreditOrgAccountRestrictionRepository,
                             HttpSafeUserMembershipRetrievalService membershipRetrievalService) {
        this.noteCreditRepository = noteCreditRepository;
        this.noteCreditUserRestrictionRepository = noteCreditUserRestrictionRepository;
        this.noteCreditOrgAccountRestrictionRepository = noteCreditOrgAccountRestrictionRepository;
        this.membershipRetrievalService = membershipRetrievalService;
    }

    public NoteCreditResponseModel getCredits() {
        UserEntity loggedInUser = membershipRetrievalService.getAuthenticatedUserEntity();
        MembershipEntity userMembership = loggedInUser.getMembershipEntity();
        NoteCreditEntity foundNoteCredits = noteCreditRepository.findAccessibleNoteCredits(userMembership.getOrganizationAccountId(), loggedInUser.getUserId())
                .orElseGet(() -> initializeNoteCredits(loggedInUser, userMembership));

        UUID orgRestrictionId = Optional.ofNullable(foundNoteCredits.getNoteCreditOrgAccountRestrictionEntity())
                .map(NoteCreditOrgAccountRestrictionEntity::getOrganizationAccountId)
                .orElse(null);
        UUID userRestrictionId = Optional.ofNullable(foundNoteCredits.getNoteCreditUserRestrictionEntity())
                .map(NoteCreditUserRestrictionEntity::getUserId)
                .orElse(null);
        RestrictionModel restrictionModel = new RestrictionModel(orgRestrictionId, userRestrictionId);
        return new NoteCreditResponseModel(foundNoteCredits.getNoteCreditId(), foundNoteCredits.getAvailableCredits(), restrictionModel);
    }

    private NoteCreditEntity initializeNoteCredits(UserEntity loggedInUser, MembershipEntity userMembership) {
        NoteCreditEntity noteCreditsToSave = new NoteCreditEntity(null, 0);
        NoteCreditEntity savedNoteCredits = noteCreditRepository.save(noteCreditsToSave);
        if (membershipRetrievalService.isAuthenticateUserBasicOrPremiumMember()) {
            NoteCreditUserRestrictionEntity userRestrictionToSave = new NoteCreditUserRestrictionEntity(savedNoteCredits.getNoteCreditId(), loggedInUser.getUserId());
            NoteCreditUserRestrictionEntity savedUserRestriction = noteCreditUserRestrictionRepository.save(userRestrictionToSave);
            savedNoteCredits.setNoteCreditUserRestrictionEntity(savedUserRestriction);
        } else {
            NoteCreditOrgAccountRestrictionEntity orgAcctRestrictionToSave = new NoteCreditOrgAccountRestrictionEntity(savedNoteCredits.getNoteCreditId(), userMembership.getOrganizationAccountId());
            NoteCreditOrgAccountRestrictionEntity savedOrgAcctRestriction = noteCreditOrgAccountRestrictionRepository.save(orgAcctRestrictionToSave);
            savedNoteCredits.setNoteCreditOrgAccountRestrictionEntity(savedOrgAcctRestriction);
        }
        return savedNoteCredits;
    }

}
