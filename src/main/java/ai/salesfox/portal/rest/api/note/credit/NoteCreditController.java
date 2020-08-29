package ai.salesfox.portal.rest.api.note.credit;

import ai.salesfox.portal.rest.api.note.credit.model.NoteCreditResponseModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public NoteCreditResponseModel getNoteCredits() {
        return noteCreditService.getCredits();
    }

}
