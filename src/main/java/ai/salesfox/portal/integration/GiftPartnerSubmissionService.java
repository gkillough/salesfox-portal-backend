package ai.salesfox.portal.integration;

import ai.salesfox.integration.common.exception.SalesfoxException;
import ai.salesfox.portal.database.account.entity.UserEntity;
import ai.salesfox.portal.database.gift.GiftEntity;
import ai.salesfox.portal.integration.scribeless.workflow.ScribelessSoloNoteManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class GiftPartnerSubmissionService {
    private final ScribelessSoloNoteManager scribelessSoloNoteManager;

    @Autowired
    public GiftPartnerSubmissionService(ScribelessSoloNoteManager scribelessSoloNoteManager) {
        this.scribelessSoloNoteManager = scribelessSoloNoteManager;
    }

    public void submitGiftToPartners(GiftEntity gift, UserEntity submittingUser) throws SalesfoxException {
        boolean hasNote = null != gift.getGiftNoteDetailEntity();
        boolean hasItem = null != gift.getGiftItemDetailEntity();

        if (hasNote && hasItem) {
            submitGiftWithNote();
        } else if (hasNote) {
            scribelessSoloNoteManager.submitNoteToScribeless(gift, submittingUser);
        } else if (hasItem) {
            submitSoloGift();
        } else {
            log.warn("A gift with id=[{}] was seemingly submitted without an item or a note", gift.getGiftId());
        }
    }

    private void submitSoloGift() {
        // FIXME implement
    }

    private void submitGiftWithNote() {
        // FIXME implement
    }

}
