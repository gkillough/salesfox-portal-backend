package ai.salesfox.portal.task.gift;

import ai.salesfox.portal.task.PortalTask;
import org.springframework.stereotype.Component;

@Component
public class SubmitScheduledGiftTask extends PortalTask {
    public static final String TASK_KEY = "task.scheduled.gift";

    public SubmitScheduledGiftTask() {
        super(TASK_KEY);
    }

    @Override
    public void runTask() {
        // FIXME implement
    }

}
