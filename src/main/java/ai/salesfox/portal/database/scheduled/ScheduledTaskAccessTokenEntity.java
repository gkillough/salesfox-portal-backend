package ai.salesfox.portal.database.scheduled;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(schema = "portal", name = "scheduled_task_access_tokens")
public class ScheduledTaskAccessTokenEntity {
    @Id
    @PrimaryKeyJoinColumn
    @Column(name = "task_id")
    private UUID taskId;

    @Column(name = "access_token")
    private String accessToken;

}
