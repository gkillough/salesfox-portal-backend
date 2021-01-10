package ai.salesfox.portal.rest.api.gift;

import ai.salesfox.portal.rest.api.common.page.PageMetadata;
import ai.salesfox.portal.rest.api.gift.model.*;
import ai.salesfox.portal.rest.api.gift.scheduling.GiftScheduleRequestModel;
import ai.salesfox.portal.rest.api.gift.scheduling.GiftScheduleResponseModel;
import ai.salesfox.portal.rest.api.gift.scheduling.GiftSchedulingEndpointService;
import ai.salesfox.portal.rest.security.authorization.PortalAuthorityConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(GiftEndpointConstants.BASE_ENDPOINT)
public class GiftController {
    private final GiftService giftService;
    private final GiftProcessingService giftProcessingService;
    private final GiftSchedulingEndpointService giftSchedulingEndpointService;

    @Autowired
    public GiftController(GiftService giftService, GiftProcessingService giftProcessingService, GiftSchedulingEndpointService giftSchedulingEndpointService) {
        this.giftService = giftService;
        this.giftProcessingService = giftProcessingService;
        this.giftSchedulingEndpointService = giftSchedulingEndpointService;
    }

    // General

    @GetMapping
    public MultiGiftModel getGifts(
            @RequestParam(defaultValue = PageMetadata.DEFAULT_OFFSET_STRING) Integer offset,
            @RequestParam(defaultValue = PageMetadata.DEFAULT_LIMIT_STRING) Integer limit,
            @RequestParam(required = false) List<String> statuses
    ) {
        return giftService.getGifts(offset, limit, statuses);
    }

    @GetMapping("/{giftId}")
    public GiftResponseModel getGift(@PathVariable UUID giftId) {
        return giftService.getGift(giftId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GiftResponseModel createDraftGift(@RequestBody DraftGiftRequestModel requestModel) {
        return giftService.createDraftGift(requestModel);
    }

    @PutMapping("/{giftId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateDraftGift(@PathVariable UUID giftId, @RequestBody DraftGiftRequestModel requestModel) {
        giftService.updateDraftGift(giftId, requestModel);
    }

    @PostMapping("/{giftId}/discard")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void discardDraftGift(@PathVariable UUID giftId) {
        giftService.discardDraftGift(giftId);
    }

    // Processing

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

    // Scheduling

    @PostMapping("/{giftId}/schedule")
    @ResponseStatus(HttpStatus.CREATED)
    public GiftScheduleResponseModel scheduleGiftDraftSubmission(@PathVariable UUID giftId, @RequestBody GiftScheduleRequestModel requestModel) {
        return giftSchedulingEndpointService.scheduleGiftDraftSubmission(giftId, requestModel);
    }

    @PutMapping("/{giftId}/schedule")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateGiftSchedule(@PathVariable UUID giftId, @RequestBody GiftScheduleRequestModel requestModel) {
        giftSchedulingEndpointService.updateGiftSchedule(giftId, requestModel);
    }

    @DeleteMapping("/{giftId}/schedule")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unscheduleGift(@PathVariable UUID giftId) {
        giftSchedulingEndpointService.unscheduleGift(giftId);
    }

}
