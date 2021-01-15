package ai.salesfox.portal.task.api;

import ai.salesfox.portal.database.scheduled.ScheduledTaskAccessTokenEntity;
import ai.salesfox.portal.database.scheduled.ScheduledTaskAccessTokenRepository;
import ai.salesfox.portal.database.scheduled.ScheduledTaskEntity;
import ai.salesfox.portal.database.scheduled.ScheduledTaskRepository;
import ai.salesfox.portal.task.PortalTaskRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class PortalTaskEndpointService {
    private final ScheduledTaskRepository scheduledTaskRepository;
    private final ScheduledTaskAccessTokenRepository scheduledTaskAccessTokenRepository;
    private final PortalTaskRegistry portalTaskRegistry;

    @Autowired
    public PortalTaskEndpointService(ScheduledTaskRepository scheduledTaskRepository, ScheduledTaskAccessTokenRepository scheduledTaskAccessTokenRepository, PortalTaskRegistry portalTaskRegistry) {
        this.scheduledTaskRepository = scheduledTaskRepository;
        this.scheduledTaskAccessTokenRepository = scheduledTaskAccessTokenRepository;
        this.portalTaskRegistry = portalTaskRegistry;
    }

    public MultiPortalTaskResponseModel getTasks() {
        List<PortalTaskResponseModel> taskModels = scheduledTaskRepository.findAll()
                .stream()
                .map(this::convertToResponseModel)
                .collect(Collectors.toList());
        return new MultiPortalTaskResponseModel(taskModels);
    }

    // Since this will be exposed publicly only return BAD_REQUEST for failed attempts
    public void runTask(String taskIdOrKey, String accessToken) {
        ScheduledTaskAccessTokenEntity taskAccessTokenEntry = scheduledTaskAccessTokenRepository.findByAccessToken(accessToken)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST));
        ScheduledTaskEntity scheduledTaskEntity = scheduledTaskRepository.findByTaskIdOrKey(taskIdOrKey)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST));
        if (!taskAccessTokenEntry.getTaskId().equals(scheduledTaskEntity.getTaskId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        boolean didTaskRun = portalTaskRegistry.runAsyncTask(scheduledTaskEntity.getTaskId());
        if (!didTaskRun) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to run scheduled task");
        }
    }

    private PortalTaskResponseModel convertToResponseModel(ScheduledTaskEntity taskEntity) {
        return new PortalTaskResponseModel(taskEntity.getTaskId(), taskEntity.getKey(), taskEntity.getLastRun());
    }

}
