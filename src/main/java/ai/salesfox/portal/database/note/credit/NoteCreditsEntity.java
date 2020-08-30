package ai.salesfox.portal.database.note.credit;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.util.UUID;

@Data
@NoArgsConstructor
@Entity
@Table(schema = "portal", name = "note_credits")
public class NoteCreditsEntity implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "note_credit_id")
    private UUID noteCreditId;

    @Column(name = "available_credits")
    private Integer availableCredits;

    @OneToOne
    @JoinColumn(name = "note_credit_id", referencedColumnName = "note_credit_id", insertable = false, updatable = false)
    private NoteCreditOrgAccountRestrictionEntity noteCreditOrgAccountRestrictionEntity;

    @OneToOne
    @JoinColumn(name = "note_credit_id", referencedColumnName = "note_credit_id", insertable = false, updatable = false)
    private NoteCreditUserRestrictionEntity noteCreditUserRestrictionEntity;

    public NoteCreditsEntity(UUID noteCreditId, Integer availableCredits) {
        this.noteCreditId = noteCreditId;
        this.availableCredits = availableCredits;
    }

}
