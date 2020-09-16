package ai.salesfox.portal.rest.api.gift.recipient;

import ai.salesfox.portal.rest.api.common.page.PageMetadata;
import ai.salesfox.portal.rest.api.gift.GiftController;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@RestController
@RequestMapping(GiftRecipientController.BASE_ENDPOINT)
public class GiftRecipientController {
    public static final String BASE_ENDPOINT = GiftController.BASE_ENDPOINT + "/{giftId}/recipients";

    @GetMapping
    public MultiGiftRecipientResponseModel getRecipients(@PathVariable UUID giftId, @RequestParam(defaultValue = PageMetadata.DEFAULT_OFFSET_STRING) Integer offset, @RequestParam(defaultValue = PageMetadata.DEFAULT_LIMIT_STRING) Integer limit) {
        // FIXME implement
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void setRecipients(@PathVariable UUID giftId, @RequestBody GiftRecipientRequestModel recipientRequest) {
        // FIXME implement
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED);
    }

    @PutMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void appendRecipients(@PathVariable UUID giftId, @RequestBody GiftRecipientRequestModel recipientRequest) {
        // FIXME implement
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteRecipients(@PathVariable UUID giftId, @RequestBody GiftRecipientRequestModel recipientRequest) {
        // FIXME implement
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void clearAllRecipients(@PathVariable UUID giftId) {
        // FIXME implement
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED);
    }

}
