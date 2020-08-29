package ai.salesfox.portal.database.note.credit;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(schema = "portal", name = "note_credit_user_restrictions")
public class NoteCreditUserRestrictionEntity implements Serializable {
    @Id
    @Column(name = "note_credit_id")
    private UUID noteCreditId;

    @PrimaryKeyJoinColumn
    @Column(name = "user_id")
    private UUID userId;

}
