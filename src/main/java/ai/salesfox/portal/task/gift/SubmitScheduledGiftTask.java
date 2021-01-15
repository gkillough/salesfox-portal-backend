package ai.salesfox.portal.task.gift;

import ai.salesfox.portal.task.PortalTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SubmitScheduledGiftTask extends PortalTask {
    public static final String TASK_KEY = "task.scheduled.gift";

    private final SubmitScheduledGiftsTaskRunner submitScheduledGiftsTaskRunner;

    @Autowired
    public SubmitScheduledGiftTask(SubmitScheduledGiftsTaskRunner submitScheduledGiftsTaskRunner) {
        super(TASK_KEY);
        this.submitScheduledGiftsTaskRunner = submitScheduledGiftsTaskRunner;
    }

    @Override
    // TODO invoke from endpoint
    public void runTask() {
        submitScheduledGiftsTaskRunner.submitScheduledGifts();
    }

}
