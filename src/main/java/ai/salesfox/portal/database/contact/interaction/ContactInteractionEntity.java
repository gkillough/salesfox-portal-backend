package ai.salesfox.portal.database.contact.interaction;

import ai.salesfox.portal.database.account.entity.UserEntity;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@Entity
@Table(schema = "portal", name = "contact_interactions")
public class ContactInteractionEntity implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "interaction_id")
    private UUID interactionId;

    @PrimaryKeyJoinColumn
    @Column(name = "contact_id")
    private UUID contactId;

    @PrimaryKeyJoinColumn
    @Column(name = "interacting_user_id")
    private UUID interactingUserId;

    @Column(name = "medium")
    private String medium;

    @Column(name = "classification")
    private String classification;

    @Column(name = "date")
    private LocalDate date;

    @Column(name = "note")
    private String note;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "interacting_user_id", referencedColumnName = "user_id", insertable = false, updatable = false)
    private UserEntity userEntity;

    public ContactInteractionEntity(UUID interactionId, UUID contactId, UUID interactingUserId, String medium, String classification, LocalDate date, String note) {
        this.interactionId = interactionId;
        this.contactId = contactId;
        this.interactingUserId = interactingUserId;
        this.medium = medium;
        this.classification = classification;
        this.date = date;
        this.note = note;
    }

}
