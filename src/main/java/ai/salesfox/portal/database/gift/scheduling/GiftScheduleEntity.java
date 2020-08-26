package ai.salesfox.portal.database.gift.scheduling;

import ai.salesfox.portal.database.account.entity.UserEntity;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@Entity
@Table(schema = "portal", name = "gift_schedules")
public class GiftScheduleEntity {
    @Id
    @PrimaryKeyJoinColumn
    @Column(name = "gift_id")
    private UUID giftId;

    @Column(name = "send_date")
    private LocalDate sendDate;

    @Column(name = "scheduling_user_id")
    private UUID schedulingUserId;

    @OneToOne
    @JoinColumn(name = "scheduling_user_id", referencedColumnName = "user_id", updatable = false, insertable = false)
    private UserEntity schedulingUserEntity;

    public GiftScheduleEntity(UUID giftId, LocalDate sendDate, UUID schedulingUserId) {
        this.giftId = giftId;
        this.sendDate = sendDate;
        this.schedulingUserId = schedulingUserId;
    }

}
