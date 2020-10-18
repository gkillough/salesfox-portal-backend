package ai.salesfox.portal.database.campaign;

import ai.salesfox.portal.database.account.entity.UserEntity;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@Entity
@Table(schema = "portal", name = "user_campaign_send_dates")
public class UserCampaignSendDateEntity {
    @Id
    @PrimaryKeyJoinColumn
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "user_campaign_id")
    private UUID userCampaignId;

    @PrimaryKeyJoinColumn
    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "date")
    private LocalDate date;

    @Column(name = "recipient_count")
    private Integer recipientCount;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "user_id", updatable = false, insertable = false)
    private UserEntity userEntity;

    public UserCampaignSendDateEntity(UUID userCampaignId, UUID userId, LocalDate date, Integer recipientCount) {
        this.userCampaignId = userCampaignId;
        this.userId = userId;
        this.date = date;
        this.recipientCount = recipientCount;
    }

}
