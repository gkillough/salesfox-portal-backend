package ai.salesfox.portal.task;

import ai.salesfox.portal.common.enumeration.GiftTrackingStatus;
import ai.salesfox.portal.common.thread.ExecutorConfiguration;
import ai.salesfox.portal.database.gift.GiftEntity;
import ai.salesfox.portal.database.gift.GiftRepository;
import ai.salesfox.portal.database.gift.scheduling.GiftScheduleEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Optional;
import java.util.concurrent.*;

@Slf4j
@Component
public class ScheduledGiftAsyncSubmitter {
    private static final int DEFAULT_PAGE_SIZE = 500;

    private final GiftRepository giftRepository;
    private final ScheduledGiftSubmissionService giftSubmissionUtility;
    private final ExecutorService maxThreadPoolExecutorService;

    @Autowired
    public ScheduledGiftAsyncSubmitter(GiftRepository giftRepository, ScheduledGiftSubmissionService giftSubmissionUtility, @Qualifier(ExecutorConfiguration.DEFAULT_EXECUTOR_SERVICE_NAME) ExecutorService maxThreadPoolExecutorService) {
        this.giftRepository = giftRepository;
        this.giftSubmissionUtility = giftSubmissionUtility;
        this.maxThreadPoolExecutorService = maxThreadPoolExecutorService;
    }

    @Async(ExecutorConfiguration.SINGLE_THREADED_EXECUTOR_SERVICE_NAME)
    public final void submitScheduledGifts() {
        int pageNumber = 0;
        PageRequest pageRequest;
        Slice<GiftEntity> scheduledGiftBatch;

        int numberOfGiftsSubmitted = 0;
        CompletionService<Optional<GiftEntity>> completionService = new ExecutorCompletionService<>(maxThreadPoolExecutorService);
        do {
            pageRequest = PageRequest.of(pageNumber++, DEFAULT_PAGE_SIZE);
            scheduledGiftBatch = giftRepository.findScheduledGiftsBySendDate(GiftTrackingStatus.SCHEDULED.name(), LocalDate.now(), pageRequest);
            sendBatchOfGifts(completionService, scheduledGiftBatch);
            numberOfGiftsSubmitted += scheduledGiftBatch.getSize();
        } while (scheduledGiftBatch.hasNext());

        handleGiftSubmissionCompletion(numberOfGiftsSubmitted, completionService);
    }

    private void sendBatchOfGifts(CompletionService<Optional<GiftEntity>> completionService, Slice<GiftEntity> batchOfGifts) {
        for (GiftEntity giftToSubmit : batchOfGifts) {
            GiftScheduleEntity giftSchedule = giftToSubmit.getGiftScheduleEntity();
            Callable<Optional<GiftEntity>> giftSubmissionCallable = () -> giftSubmissionUtility.submitGift(giftToSubmit, giftSchedule.getSchedulingUserEntity());
            completionService.submit(giftSubmissionCallable);
        }
    }

    private void handleGiftSubmissionCompletion(int numberOfGiftsSubmitted, CompletionService<Optional<GiftEntity>> completionService) {
        for (int i = 0; i < numberOfGiftsSubmitted; i++) {
            try {
                handleGiftSubmissionResult(completionService.take());
            } catch (InterruptedException e) {
                log.error("The thread was interrupted while waiting to get a completed gift submission", e);
                Thread.currentThread().interrupt();
            }
        }
    }

    private void handleGiftSubmissionResult(Future<Optional<GiftEntity>> giftSubmissionResult) {
        try {
            giftSubmissionResult.get(3L, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            log.error("The thread was interrupted while waiting for a gift submission to complete", e);
            Thread.currentThread().interrupt();
        } catch (ExecutionException e) {
            log.error("The thread was interrupted while waiting for a gift submission to complete", e);
        } catch (TimeoutException e) {
            log.error("The gift submission task timed-out", e);
        }
    }

}
