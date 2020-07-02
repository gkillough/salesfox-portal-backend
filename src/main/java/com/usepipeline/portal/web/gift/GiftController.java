package com.usepipeline.portal.web.gift;

import com.usepipeline.portal.web.common.page.PageMetadata;
import com.usepipeline.portal.web.gift.model.*;
import com.usepipeline.portal.web.security.authorization.PortalAuthorityConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(GiftController.BASE_ENDPOINT)
public class GiftController {
    public static final String BASE_ENDPOINT = "/gifts";

    private GiftService giftService;
    private GiftProcessingService giftProcessingService;

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

    @PostMapping("/{giftId}/send")
    public GiftResponseModel sendGift(@PathVariable UUID giftId) {
        return giftProcessingService.sendGift(giftId);
    }

    @PostMapping("/{giftId}/status")
    @PreAuthorize(PortalAuthorityConstants.PIPELINE_ADMIN_AUTH_CHECK)
    public GiftResponseModel updateGiftStatus(@PathVariable UUID giftId, @RequestBody UpdateGiftStatusRequestModel requestModel) {
        return giftProcessingService.updateGiftStatus(giftId, requestModel);
    }

    @PostMapping("/{giftId}/tracking")
    @PreAuthorize(PortalAuthorityConstants.PIPELINE_ADMIN_AUTH_CHECK)
    public GiftResponseModel updateGiftTrackingDetail(@PathVariable UUID giftId, @RequestBody UpdateGiftTrackingDetailRequestModel requestModel) {
        return giftProcessingService.updateGiftTrackingDetail(giftId, requestModel);
    }

}
