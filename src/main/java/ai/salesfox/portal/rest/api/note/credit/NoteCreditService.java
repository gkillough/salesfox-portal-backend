package ai.salesfox.portal.rest.api.note.credit;

import ai.salesfox.portal.database.account.entity.MembershipEntity;
import ai.salesfox.portal.database.account.entity.UserEntity;
import ai.salesfox.portal.database.note.credit.*;
import ai.salesfox.portal.integration.stripe.StripeService;
import ai.salesfox.portal.rest.api.common.model.request.RestrictionModel;
import ai.salesfox.portal.rest.api.note.credit.model.NoteCreditsRequestModel;
import ai.salesfox.portal.rest.api.note.credit.model.NoteCreditsResponseModel;
import ai.salesfox.portal.rest.util.HttpSafeUserMembershipRetrievalService;
import com.stripe.model.Charge;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;

@Component
public class NoteCreditService {
    private final NoteCreditsRepository noteCreditsRepository;
    private final NoteCreditsUserRestrictionRepository noteCreditsUserRestrictionRepository;
    private final NoteCreditsOrgAccountRestrictionRepository noteCreditsOrgAccountRestrictionRepository;
    private final HttpSafeUserMembershipRetrievalService membershipRetrievalService;
    private final StripeService stripeService;

    @Autowired
    public NoteCreditService(NoteCreditsRepository noteCreditsRepository, NoteCreditsUserRestrictionRepository noteCreditsUserRestrictionRepository, NoteCreditsOrgAccountRestrictionRepository noteCreditsOrgAccountRestrictionRepository,
                             HttpSafeUserMembershipRetrievalService membershipRetrievalService, StripeService stripeService) {
        this.noteCreditsRepository = noteCreditsRepository;
        this.noteCreditsUserRestrictionRepository = noteCreditsUserRestrictionRepository;
        this.noteCreditsOrgAccountRestrictionRepository = noteCreditsOrgAccountRestrictionRepository;
        this.membershipRetrievalService = membershipRetrievalService;
        this.stripeService = stripeService;
    }

    public NoteCreditsResponseModel getCredits() {
        NoteCreditsEntity foundNoteCredits = findNoteCredits();
        return createResponseModel(foundNoteCredits);
    }

    @Transactional
    public String orderCredits(NoteCreditsRequestModel requestModel) {
        final int notePrice = 4;
        NoteCreditsEntity foundNoteCredits = findNoteCredits();
        String token = requestModel.getToken();
        Charge charge;
        membershipRetrievalService.getAuthenticatedUserEntity().getMembershipEntity().getOrganizationAccountId();
        Integer requestedQuantity = requestModel.getQuantity();
        if (null == requestedQuantity) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The field 'quantity' is required");
        } else if (requestedQuantity < 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The requested quantity must be greater than zero");
        }
        try {
            charge = stripeService.chargeNewCard(token, requestedQuantity * notePrice);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid card or insufficient funds");
        }
        if (charge == null) {
            return "Failure";
        } else {
            int newQuantity = foundNoteCredits.getAvailableCredits() + requestedQuantity;
            foundNoteCredits.setAvailableCredits(newQuantity);
            noteCreditsRepository.save(foundNoteCredits);
            return "Success";
        }

    }

    private NoteCreditsEntity findNoteCredits() {
        UserEntity loggedInUser = membershipRetrievalService.getAuthenticatedUserEntity();
        MembershipEntity userMembership = loggedInUser.getMembershipEntity();
        return noteCreditsRepository.findAccessibleNoteCredits(userMembership.getOrganizationAccountId(), loggedInUser.getUserId())
                .orElseGet(() -> initializeNoteCredits(loggedInUser, userMembership));
    }

    private NoteCreditsEntity initializeNoteCredits(UserEntity loggedInUser, MembershipEntity userMembership) {
        NoteCreditsEntity noteCreditsToSave = new NoteCreditsEntity(null, 0);
        NoteCreditsEntity savedNoteCredits = noteCreditsRepository.save(noteCreditsToSave);
        if (membershipRetrievalService.isAuthenticateUserBasicOrPremiumMember()) {
            NoteCreditUserRestrictionEntity userRestrictionToSave = new NoteCreditUserRestrictionEntity(savedNoteCredits.getNoteCreditId(), loggedInUser.getUserId());
            NoteCreditUserRestrictionEntity savedUserRestriction = noteCreditsUserRestrictionRepository.save(userRestrictionToSave);
            savedNoteCredits.setNoteCreditUserRestrictionEntity(savedUserRestriction);
        } else {
            NoteCreditOrgAccountRestrictionEntity orgAcctRestrictionToSave = new NoteCreditOrgAccountRestrictionEntity(savedNoteCredits.getNoteCreditId(), userMembership.getOrganizationAccountId());
            NoteCreditOrgAccountRestrictionEntity savedOrgAcctRestriction = noteCreditsOrgAccountRestrictionRepository.save(orgAcctRestrictionToSave);
            savedNoteCredits.setNoteCreditOrgAccountRestrictionEntity(savedOrgAcctRestriction);
        }
        return savedNoteCredits;
    }

    private NoteCreditsResponseModel createResponseModel(NoteCreditsEntity noteCredits) {
        UUID orgRestrictionId = Optional.ofNullable(noteCredits.getNoteCreditOrgAccountRestrictionEntity())
                .map(NoteCreditOrgAccountRestrictionEntity::getOrganizationAccountId)
                .orElse(null);
        UUID userRestrictionId = Optional.ofNullable(noteCredits.getNoteCreditUserRestrictionEntity())
                .map(NoteCreditUserRestrictionEntity::getUserId)
                .orElse(null);
        RestrictionModel restrictionModel = new RestrictionModel(orgRestrictionId, userRestrictionId);
        return new NoteCreditsResponseModel(noteCredits.getNoteCreditId(), noteCredits.getAvailableCredits(), restrictionModel);
    }

}
