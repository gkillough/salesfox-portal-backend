package ai.salesfox.portal.database.note.icon;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(schema = "portal", name = "note_custom_icons")
public class NoteCustomIconEntity {
    @Id
    @Column(name = "note_id")
    private UUID noteId;

    @PrimaryKeyJoinColumn
    @Column(name = "custom_icon_id")
    private UUID customIconId;

}
