package ai.salesfox.portal.rest.api.gift.recipient;

import ai.salesfox.portal.rest.api.common.page.PageMetadata;
import ai.salesfox.portal.rest.api.gift.GiftController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(GiftRecipientController.BASE_ENDPOINT)
public class GiftRecipientController {
    public static final String BASE_ENDPOINT = GiftController.BASE_ENDPOINT + "/{giftId}/recipients";

    private final GiftRecipientEndpointService giftRecipientEndpointService;

    @Autowired
    public GiftRecipientController(GiftRecipientEndpointService giftRecipientEndpointService) {
        this.giftRecipientEndpointService = giftRecipientEndpointService;
    }

    @GetMapping
    public MultiGiftRecipientResponseModel getRecipients(@PathVariable UUID giftId, @RequestParam(defaultValue = PageMetadata.DEFAULT_OFFSET_STRING) Integer offset, @RequestParam(defaultValue = PageMetadata.DEFAULT_LIMIT_STRING) Integer limit) {
        return giftRecipientEndpointService.getRecipients(giftId, offset, limit);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void setRecipients(@PathVariable UUID giftId, @RequestBody GiftRecipientRequestModel recipientRequest) {
        giftRecipientEndpointService.setRecipients(giftId, recipientRequest);
    }

    @PutMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void appendRecipients(@PathVariable UUID giftId, @RequestBody GiftRecipientRequestModel recipientRequest) {
        giftRecipientEndpointService.appendRecipients(giftId, recipientRequest);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteRecipients(@PathVariable UUID giftId, @RequestBody GiftRecipientRequestModel recipientRequest) {
        giftRecipientEndpointService.deleteRecipients(giftId, recipientRequest);
    }

}
