package ai.salesfox.portal.task;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public abstract class SalesfoxTask {
    @Getter
    private final String key;

    public abstract void runTask();

}
