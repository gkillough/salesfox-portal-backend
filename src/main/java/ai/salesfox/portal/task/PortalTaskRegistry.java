package ai.salesfox.portal.task;

import ai.salesfox.portal.database.scheduled.ScheduledTaskEntity;
import ai.salesfox.portal.database.scheduled.ScheduledTaskRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Configuration
public class PortalTaskRegistry {
    private final List<PortalTask> tasks;
    private final ScheduledTaskRepository scheduledTaskRepository;

    @Autowired
    public PortalTaskRegistry(List<PortalTask> tasks, ScheduledTaskRepository scheduledTaskRepository) {
        this.tasks = tasks;
        this.scheduledTaskRepository = scheduledTaskRepository;
    }

    @PostConstruct
    @Transactional
    public void registerTasks() {
        List<ScheduledTaskEntity> allTaskEntities = scheduledTaskRepository.findAll();
        Set<String> trackedTaskKeys = allTaskEntities
                .stream()
                .map(ScheduledTaskEntity::getKey)
                .collect(Collectors.toSet());

        List<ScheduledTaskEntity> tasksToAdd = new ArrayList<>(tasks.size());
        for (PortalTask task : tasks) {
            String taskKey = task.getKey();
            if (!trackedTaskKeys.contains(taskKey)) {
                log.info("Adding task to database: key=[{}]", taskKey);
                tasksToAdd.add(new ScheduledTaskEntity(null, taskKey));
            }
        }
        scheduledTaskRepository.saveAll(tasksToAdd);
    }

}
