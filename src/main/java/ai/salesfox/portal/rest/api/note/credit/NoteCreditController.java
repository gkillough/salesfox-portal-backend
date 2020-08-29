package ai.salesfox.portal.rest.api.note.credit;

import ai.salesfox.portal.rest.api.note.credit.model.NoteCreditsRequestModel;
import ai.salesfox.portal.rest.api.note.credit.model.NoteCreditsResponseModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(NoteCreditController.BASE_CONTROLLER)
public class NoteCreditController {
    public static final String BASE_CONTROLLER = "/note_credits";

    private final NoteCreditService noteCreditService;

    @Autowired
    public NoteCreditController(NoteCreditService noteCreditService) {
        this.noteCreditService = noteCreditService;
    }

    @GetMapping
    public NoteCreditsResponseModel getNoteCredits() {
        return noteCreditService.getCredits();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void orderNoteCredits(@RequestBody NoteCreditsRequestModel requestModel) {
        noteCreditService.orderCredits(requestModel);
    }

}
