package ai.salesfox.portal.common.service.note;

import ai.salesfox.integration.common.function.ThrowingConsumer;
import ai.salesfox.portal.database.note.credit.NoteCreditsEntity;
import ai.salesfox.portal.database.note.credit.NoteCreditsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class NoteCreditAvailabilityService {
    private final NoteCreditsRepository noteCreditsRepository;

    @Autowired
    public NoteCreditAvailabilityService(NoteCreditsRepository noteCreditsRepository) {
        this.noteCreditsRepository = noteCreditsRepository;
    }

    public void incrementNoteCredits(NoteCreditsEntity noteCredit) {
        incrementNoteCredits(noteCredit, 1);
    }

    public void incrementNoteCredits(NoteCreditsEntity noteCredit, int quantity) {
        noteCredit.setAvailableCredits(noteCredit.getAvailableCredits() + quantity);
        noteCreditsRepository.save(noteCredit);
    }

    public <E extends Throwable> boolean decrementNoteCreditsOrElse(NoteCreditsEntity noteCredits, int quantity, ThrowingConsumer<NoteCreditsEntity, E> outOfCreditsHandler) throws E {
        if (noteCredits.getAvailableCredits() < quantity) {
            outOfCreditsHandler.accept(noteCredits);
            return false;
        }
        incrementNoteCredits(noteCredits, -quantity);
        return true;
    }

}
