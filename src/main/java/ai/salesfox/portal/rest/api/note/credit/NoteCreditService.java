package ai.salesfox.portal.rest.api.note.credit;

import ai.salesfox.portal.common.exception.PortalException;
import ai.salesfox.portal.database.account.entity.MembershipEntity;
import ai.salesfox.portal.database.account.entity.UserEntity;
import ai.salesfox.portal.database.note.credit.*;
import ai.salesfox.portal.integration.stripe.StripeChargeService;
import ai.salesfox.portal.rest.api.common.model.request.RestrictionModel;
import ai.salesfox.portal.rest.api.note.credit.model.NoteCreditsRequestModel;
import ai.salesfox.portal.rest.api.note.credit.model.NoteCreditsResponseModel;
import ai.salesfox.portal.rest.util.HttpSafeUserMembershipRetrievalService;
import com.stripe.model.Charge;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Component
public class NoteCreditService {
    private final NoteCreditsRepository noteCreditsRepository;
    private final NoteCreditPriceRepository noteCreditPriceRepository;
    private final NoteCreditsOrgAccountRestrictionRepository noteCreditsOrgAccountRestrictionRepository;
    private final HttpSafeUserMembershipRetrievalService membershipRetrievalService;
    private final StripeChargeService stripeChargeService;

    @Autowired
    public NoteCreditService(
            NoteCreditsRepository noteCreditsRepository,
            NoteCreditPriceRepository noteCreditPriceRepository,
            NoteCreditsOrgAccountRestrictionRepository noteCreditsOrgAccountRestrictionRepository,
            HttpSafeUserMembershipRetrievalService membershipRetrievalService,
            StripeChargeService stripeChargeService
    ) {
        this.noteCreditsRepository = noteCreditsRepository;
        this.noteCreditPriceRepository = noteCreditPriceRepository;
        this.noteCreditsOrgAccountRestrictionRepository = noteCreditsOrgAccountRestrictionRepository;
        this.membershipRetrievalService = membershipRetrievalService;
        this.stripeChargeService = stripeChargeService;
    }

    public NoteCreditsResponseModel getCredits() {
        UserEntity loggedInUser = membershipRetrievalService.getAuthenticatedUserEntity();
        NoteCreditsEntity foundNoteCredits = findNoteCredits(loggedInUser);
        return createResponseModel(foundNoteCredits);
    }

    @Transactional
    public void orderCredits(NoteCreditsRequestModel requestModel) {
        UserEntity loggedInUser = membershipRetrievalService.getAuthenticatedUserEntity();
        NoteCreditsEntity foundNoteCredits = findNoteCredits(loggedInUser);

        Integer requestedQuantity = requestModel.getQuantity();
        if (null == requestedQuantity) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The field 'quantity' is required");
        } else if (requestedQuantity < 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The requested quantity must be greater than zero");
        }

        String stripeChargeToken = requestModel.getStripeChargeToken();
        if (StringUtils.isBlank(stripeChargeToken)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The field 'stripeChargeToken' is required");
        }

        NoteCreditPriceEntity noteCreditPriceEntity = findNoteCreditPrice();
        BigDecimal noteCreditPrice = noteCreditPriceEntity.getNoteCreditPrice();

        BigDecimal bigDecimalQuantity = BigDecimal.valueOf(requestedQuantity);
        BigDecimal noteCreditTotalPrice = noteCreditPrice.multiply(bigDecimalQuantity);
        double noteCreditTotalPriceDouble = noteCreditTotalPrice.doubleValue();

        String chargeDescription = String.format("Note Credits Order. Quantity: %d, Unit Price: %f, Total: %f", requestedQuantity, noteCreditPrice.doubleValue(), noteCreditTotalPriceDouble);

        Charge charge;
        try {
            charge = stripeChargeService.chargeNewCard(stripeChargeToken, noteCreditTotalPriceDouble, chargeDescription, loggedInUser.getEmail());
        } catch (PortalException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "There was a problem processing the payment: " + e.getMessage());
        }

        if (charge == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "There was a problem processing the payment");
        } else {
            int newQuantity = foundNoteCredits.getAvailableCredits() + requestedQuantity;
            foundNoteCredits.setAvailableCredits(newQuantity);
            noteCreditsRepository.save(foundNoteCredits);
        }
    }

    private NoteCreditPriceEntity findNoteCreditPrice() {
        return noteCreditPriceRepository.findAll()
                .stream()
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "The Note Credit Price could not be retrieved"));
    }

    private NoteCreditsEntity findNoteCredits(UserEntity loggedInUser) {
        MembershipEntity userMembership = loggedInUser.getMembershipEntity();
        return noteCreditsRepository.findAccessibleNoteCredits(userMembership.getOrganizationAccountId(), loggedInUser.getUserId())
                .orElseGet(() -> initializeNoteCredits(userMembership));
    }

    private NoteCreditsEntity initializeNoteCredits(MembershipEntity userMembership) {
        NoteCreditsEntity noteCreditsToSave = new NoteCreditsEntity(null, 0);
        NoteCreditsEntity savedNoteCredits = noteCreditsRepository.save(noteCreditsToSave);

        NoteCreditOrgAccountRestrictionEntity orgAcctRestrictionToSave = new NoteCreditOrgAccountRestrictionEntity(savedNoteCredits.getNoteCreditId(), userMembership.getOrganizationAccountId());
        NoteCreditOrgAccountRestrictionEntity savedOrgAcctRestriction = noteCreditsOrgAccountRestrictionRepository.save(orgAcctRestrictionToSave);
        savedNoteCredits.setNoteCreditOrgAccountRestrictionEntity(savedOrgAcctRestriction);

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
