package ai.salesfox.portal.rest.api.note.credit.price;

import ai.salesfox.portal.rest.api.note.credit.price.model.NoteCreditPriceRequestModel;
import ai.salesfox.portal.rest.api.note.credit.price.model.NoteCreditPriceResponseModel;
import ai.salesfox.portal.rest.security.authorization.PortalAuthorityConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(NoteCreditPriceController.BASE_CONTROLLER)
public class NoteCreditPriceController {
    public static final String BASE_CONTROLLER = "/note_credit_price";

    private final NoteCreditPriceService noteCreditPriceService;

    @Autowired
    public NoteCreditPriceController(NoteCreditPriceService noteCreditPriceService) {
        this.noteCreditPriceService = noteCreditPriceService;
    }

    @GetMapping
    public NoteCreditPriceResponseModel getNoteCreditPrice() {
        return noteCreditPriceService.getNoteCreditPrice();
    }

    @PutMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize(PortalAuthorityConstants.PORTAL_ADMIN_AUTH_CHECK)
    public void changeNotePrice(@RequestBody NoteCreditPriceRequestModel requestModel) {
        noteCreditPriceService.updateNoteCreditPrice(requestModel);
    }

}