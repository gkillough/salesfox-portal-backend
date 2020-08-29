package ai.salesfox.portal.common.service.note;

import ai.salesfox.portal.common.exception.ThrowingConsumer;
import ai.salesfox.portal.database.note.credit.NoteCreditEntity;
import ai.salesfox.portal.database.note.credit.NoteCreditRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class NoteCreditAvailabilityService {
    private final NoteCreditRepository noteCreditRepository;

    @Autowired
    public NoteCreditAvailabilityService(NoteCreditRepository noteCreditRepository) {
        this.noteCreditRepository = noteCreditRepository;
    }

    public void incrementNoteCredits(NoteCreditEntity noteCredit) {
        incrementNoteCredits(noteCredit, 1);
    }

    public void incrementNoteCredits(NoteCreditEntity noteCredit, int quantity) {
        noteCredit.setAvailableCredits(noteCredit.getAvailableCredits() + quantity);
        noteCreditRepository.save(noteCredit);
    }

    public <E extends Throwable> void decrementNoteCreditsOrElse(NoteCreditEntity noteCredits, ThrowingConsumer<NoteCreditEntity, E> outOfCreditsHandler) throws E {
        if (noteCredits.getAvailableCredits() < 1) {
            outOfCreditsHandler.accept(noteCredits);
        } else {
            incrementNoteCredits(noteCredits, -1);
        }
    }

}
