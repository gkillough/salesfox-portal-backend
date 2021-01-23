package ai.salesfox.portal.task.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PortalTaskResponseModel {
    private UUID taskId;
    private String key;
    private OffsetDateTime lastRun;

}
