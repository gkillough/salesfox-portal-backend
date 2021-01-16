package ai.salesfox.portal.task.api;

import ai.salesfox.portal.database.scheduled.ScheduledTaskAccessTokenEntity;
import ai.salesfox.portal.database.scheduled.ScheduledTaskAccessTokenRepository;
import ai.salesfox.portal.database.scheduled.ScheduledTaskEntity;
import ai.salesfox.portal.database.scheduled.ScheduledTaskRepository;
import ai.salesfox.portal.task.PortalTaskRegistry;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class PortalTaskEndpointService {
    public static final int ACCESS_TOKEN_LENGTH = 100;

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

    @Transactional
    // Since this will be exposed publicly only return BAD_REQUEST for failed attempts
    public void runTask(String taskIdOrKey, PortalTaskAccessTokenModel requestModel) {
        ScheduledTaskEntity scheduledTaskEntity = findByTaskIdOrKey(taskIdOrKey);

        String accessToken = requestModel.getToken();
        if (null == accessToken) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        ScheduledTaskAccessTokenEntity taskAccessTokenEntry = scheduledTaskAccessTokenRepository.findByAccessToken(accessToken)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST));
        if (!taskAccessTokenEntry.getTaskId().equals(scheduledTaskEntity.getTaskId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        boolean didTaskRun = portalTaskRegistry.runAsyncTask(scheduledTaskEntity.getTaskId());
        if (!didTaskRun) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to run scheduled task");
        }
    }

    @Transactional
    public PortalTaskAccessTokenModel generateAccessTokenAndReplaceExisting(String taskIdOrKey) {
        ScheduledTaskEntity foundTask = findByTaskIdOrKey(taskIdOrKey);

        String newAccessToken = generateRandomAccessToken();
        ScheduledTaskAccessTokenEntity accessTokenEntity = new ScheduledTaskAccessTokenEntity(foundTask.getTaskId(), newAccessToken);
        ScheduledTaskAccessTokenEntity savedAccessTokenEntity = scheduledTaskAccessTokenRepository.save(accessTokenEntity);
        return new PortalTaskAccessTokenModel(savedAccessTokenEntity.getAccessToken());
    }

    private ScheduledTaskEntity findByTaskIdOrKey(String taskIdOrKey) {
        Optional<ScheduledTaskEntity> optionalTask;
        try {
            UUID taskId = UUID.fromString(taskIdOrKey);
            optionalTask = scheduledTaskRepository.findById(taskId);
        } catch (IllegalArgumentException e) {
            optionalTask = scheduledTaskRepository.findByKey(taskIdOrKey);
        }
        return optionalTask
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    private String generateRandomAccessToken() {
        String randomString = RandomStringUtils.randomAlphanumeric(ACCESS_TOKEN_LENGTH - 2);
        return randomString + "==";
    }

    private PortalTaskResponseModel convertToResponseModel(ScheduledTaskEntity taskEntity) {
        return new PortalTaskResponseModel(taskEntity.getTaskId(), taskEntity.getKey(), taskEntity.getLastRun());
    }

}
