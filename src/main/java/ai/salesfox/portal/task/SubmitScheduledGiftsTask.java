package ai.salesfox.portal.task;

import ai.salesfox.portal.common.enumeration.GiftTrackingStatus;
import ai.salesfox.portal.common.thread.ExecutorConfiguration;
import ai.salesfox.portal.database.gift.GiftEntity;
import ai.salesfox.portal.database.gift.GiftRepository;
import ai.salesfox.portal.database.gift.scheduling.GiftScheduleEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Optional;
import java.util.concurrent.*;

@Slf4j
@Component
public class SubmitScheduledGiftsTask {
    public static final String CRON_DAILY_MIDNIGHT = "0 0 0 * * *";
    private static final int DEFAULT_PAGE_SIZE = 500;

    private final GiftRepository giftRepository;
    private final ScheduledGiftSubmissionService giftSubmissionService;
    private final ExecutorService maxThreadPoolExecutorService;

    @Autowired
    public SubmitScheduledGiftsTask(GiftRepository giftRepository, ScheduledGiftSubmissionService giftSubmissionService, ExecutorService maxThreadPoolExecutorService) {
        this.giftRepository = giftRepository;
        this.giftSubmissionService = giftSubmissionService;
        this.maxThreadPoolExecutorService = maxThreadPoolExecutorService;
    }

    @Scheduled(cron = CRON_DAILY_MIDNIGHT)
    public void submitScheduledGifts() {
        log.info("Running {} task", getClass().getSimpleName());
        submitAsynchronously(giftRepository, giftSubmissionService, maxThreadPoolExecutorService);
    }

    @Async(ExecutorConfiguration.SINGLE_THREADED_EXECUTOR_SERVICE_NAME)
    public void submitAsynchronously(GiftRepository giftRepository, ScheduledGiftSubmissionService giftSubmissionService, ExecutorService executorService) {
        int pageNumber = 0;
        PageRequest pageRequest;
        Slice<GiftEntity> scheduledGiftBatch;

        int numberOfGiftsSubmitted = 0;
        CompletionService<Optional<GiftEntity>> completionService = new ExecutorCompletionService<>(executorService);
        do {
            pageRequest = PageRequest.of(pageNumber++, DEFAULT_PAGE_SIZE);
            scheduledGiftBatch = giftRepository.findScheduledGiftsBySendDate(GiftTrackingStatus.SCHEDULED.name(), LocalDate.now(), pageRequest);
            sendBatchOfGifts(completionService, giftSubmissionService, scheduledGiftBatch);
            numberOfGiftsSubmitted += scheduledGiftBatch.getSize();
        } while (scheduledGiftBatch.hasNext());

        handleGiftSubmissionCompletion(numberOfGiftsSubmitted, completionService);
    }

    private void sendBatchOfGifts(CompletionService<Optional<GiftEntity>> completionService, ScheduledGiftSubmissionService giftSubmissionService, Slice<GiftEntity> batchOfGifts) {
        for (GiftEntity giftToSubmit : batchOfGifts) {
            GiftScheduleEntity giftSchedule = giftToSubmit.getGiftScheduleEntity();
            Callable<Optional<GiftEntity>> giftSubmissionCallable = () -> giftSubmissionService.submitGift(giftToSubmit, giftSchedule.getSchedulingUserEntity());
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
