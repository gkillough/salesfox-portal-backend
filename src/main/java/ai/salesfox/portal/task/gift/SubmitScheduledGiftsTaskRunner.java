package ai.salesfox.portal.task.gift;

import ai.salesfox.portal.common.enumeration.GiftTrackingStatus;
import ai.salesfox.portal.database.gift.GiftEntity;
import ai.salesfox.portal.database.gift.GiftRepository;
import ai.salesfox.portal.database.gift.scheduling.GiftScheduleEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Optional;
import java.util.concurrent.*;

@Slf4j
@Component
public class SubmitScheduledGiftsTaskRunner {
    public static final String CRON_DAILY_MIDNIGHT = "0 0 0 * * *";
    private static final int DEFAULT_PAGE_SIZE = 500;
    private static final int MAX_FAILED_RESULT_HANDLING_ATTEMPTS = 14400;

    private final GiftRepository giftRepository;
    private final ScheduledGiftSubmissionService giftSubmissionService;
    private final ExecutorService executorService;

    @Autowired
    public SubmitScheduledGiftsTaskRunner(GiftRepository giftRepository, ScheduledGiftSubmissionService giftSubmissionService, ExecutorService maxThreadPoolExecutorService) {
        this.giftRepository = giftRepository;
        this.giftSubmissionService = giftSubmissionService;
        this.executorService = maxThreadPoolExecutorService;
    }

    @Scheduled(cron = CRON_DAILY_MIDNIGHT)
    public void submitScheduledGifts() {
        log.info("Running {} task", getClass().getSimpleName());
        int pageNumber = 0;
        PageRequest pageRequest;
        Slice<GiftEntity> scheduledGiftBatch;

        int numberOfGiftsSubmitted = 0;
        CompletionService<Optional<GiftEntity>> completionService = new ExecutorCompletionService<>(executorService);
        do {
            pageRequest = PageRequest.of(pageNumber++, DEFAULT_PAGE_SIZE);
            scheduledGiftBatch = giftRepository.findScheduledGiftsBySendDateOnOrBefore(GiftTrackingStatus.SCHEDULED.name(), LocalDate.now(), pageRequest);
            sendBatchOfGifts(completionService, giftSubmissionService, scheduledGiftBatch);
            numberOfGiftsSubmitted += scheduledGiftBatch.getNumberOfElements();
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
        if (0 == numberOfGiftsSubmitted) {
            return;
        }

        int failedAttemptCount = 0;
        int numberOfResultsHandled = 0;
        Future<Optional<GiftEntity>> result = null;
        do {
            try {
                result = completionService.poll(250, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                log.error("The thread was interrupted while polling for a gift submission task", e);
                Thread.currentThread().interrupt();
            }

            if (null != result) {
                handleGiftSubmissionResult(result);
                numberOfResultsHandled++;
            } else {
                failedAttemptCount++;
            }
        } while (null != result || (numberOfResultsHandled < numberOfGiftsSubmitted && failedAttemptCount < MAX_FAILED_RESULT_HANDLING_ATTEMPTS));
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
