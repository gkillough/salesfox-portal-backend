package ai.salesfox.portal.task;

import ai.salesfox.portal.common.enumeration.GiftTrackingStatus;
import ai.salesfox.portal.database.gift.GiftEntity;
import ai.salesfox.portal.database.gift.GiftRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class ScheduledGiftAsyncSubmitter {
    private static final int DEFAULT_PAGE_SIZE = 500;

    private final GiftRepository giftRepository;

    @Autowired
    public ScheduledGiftAsyncSubmitter(GiftRepository giftRepository) {
        this.giftRepository = giftRepository;
    }

    @Async
    public final void submitScheduledGifts() {
        int pageNumber = 0;
        PageRequest pageRequest;
        Slice<GiftEntity> scheduledGiftBatch;
        do {
            pageRequest = PageRequest.of(pageNumber++, DEFAULT_PAGE_SIZE);
            scheduledGiftBatch = giftRepository.findScheduledGiftsBySendDate(GiftTrackingStatus.SCHEDULED.name(), LocalDate.now(), pageRequest);
            sendBatchOfGifts(scheduledGiftBatch);
        } while (scheduledGiftBatch.hasNext());
    }

    private void sendBatchOfGifts(Slice<GiftEntity> batchOfGifts) {
        // TODO implement
    }

}
