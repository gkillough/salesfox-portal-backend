package ai.salesfox.portal.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SubmitScheduledGiftsTask {
    public static final String CRON_DAILY_MIDNIGHT = "0 0 0 * * *";

    private final ScheduledGiftAsyncSubmitter scheduledGiftAsyncSubmitter;

    @Autowired
    public SubmitScheduledGiftsTask(ScheduledGiftAsyncSubmitter scheduledGiftAsyncSubmitter) {
        this.scheduledGiftAsyncSubmitter = scheduledGiftAsyncSubmitter;
    }

    @Scheduled(cron = CRON_DAILY_MIDNIGHT)
    public final void submitScheduledGifts() {
        log.info("Running {} task", getClass().getSimpleName());
        scheduledGiftAsyncSubmitter.submitScheduledGifts();
    }

}
