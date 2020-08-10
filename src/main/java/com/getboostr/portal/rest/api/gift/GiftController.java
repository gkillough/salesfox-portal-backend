package com.getboostr.portal.rest.api.gift;

import com.getboostr.portal.rest.api.common.page.PageMetadata;
import com.getboostr.portal.rest.api.gift.model.*;
import com.getboostr.portal.rest.security.authorization.PortalAuthorityConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(GiftController.BASE_ENDPOINT)
public class GiftController {
    public static final String BASE_ENDPOINT = "/gifts";

    private final GiftService giftService;
    private final GiftProcessingService giftProcessingService;

    @Autowired
    public GiftController(GiftService giftService, GiftProcessingService giftProcessingService) {
        this.giftService = giftService;
        this.giftProcessingService = giftProcessingService;
    }

    @GetMapping
    public MultiGiftModel getGifts(@RequestParam(defaultValue = PageMetadata.DEFAULT_OFFSET_STRING) Integer offset, @RequestParam(defaultValue = PageMetadata.DEFAULT_LIMIT_STRING) Integer limit) {
        return giftService.getGifts(offset, limit);
    }

    @GetMapping("/{giftId}")
    public GiftResponseModel getGift(@PathVariable UUID giftId) {
        return giftService.getGift(giftId);
    }

    @PostMapping
    public GiftResponseModel createDraftGift(@RequestBody DraftGiftRequestModel requestModel) {
        return giftService.createDraftGift(requestModel);
    }

    @PutMapping("/{giftId}")
    public void updateDraftGift(@PathVariable UUID giftId, @RequestBody DraftGiftRequestModel requestModel) {
        giftService.updateDraftGift(giftId, requestModel);
    }

    @PostMapping("/{giftId}/discard")
    public void discardDraftGift(@PathVariable UUID giftId) {
        giftService.discardDraftGift(giftId);
    }

    @PostMapping("/{giftId}/submit")
    public GiftResponseModel submitGift(@PathVariable UUID giftId) {
        return giftProcessingService.submitGift(giftId);
    }

    @PostMapping("/{giftId}/cancel")
    public GiftResponseModel cancelGift(@PathVariable UUID giftId) {
        return giftProcessingService.cancelGift(giftId);
    }

    @PostMapping("/{giftId}/status")
    @PreAuthorize(PortalAuthorityConstants.PORTAL_ADMIN_AUTH_CHECK)
    public GiftResponseModel updateGiftStatus(@PathVariable UUID giftId, @RequestBody UpdateGiftStatusRequestModel requestModel) {
        return giftProcessingService.updateGiftStatus(giftId, requestModel);
    }

    @PostMapping("/{giftId}/tracking")
    @PreAuthorize(PortalAuthorityConstants.PORTAL_ADMIN_AUTH_CHECK)
    public GiftResponseModel updateGiftTrackingDetail(@PathVariable UUID giftId, @RequestBody UpdateGiftTrackingDetailRequestModel requestModel) {
        return giftProcessingService.updateGiftTrackingDetail(giftId, requestModel);
    }

}
