package ai.salesfox.portal.database.note.icon;

import ai.salesfox.portal.database.customization.icon.CustomIconEntity;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.UUID;

@Data
@NoArgsConstructor
@Entity
@Table(schema = "portal", name = "note_custom_icons")
public class NoteCustomIconEntity {
    @Id
    @Column(name = "note_id")
    private UUID noteId;

    @PrimaryKeyJoinColumn
    @Column(name = "custom_icon_id")
    private UUID customIconId;

    @OneToOne
    @JoinColumn(name = "custom_icon_id", referencedColumnName = "custom_icon_id", insertable = false, updatable = false)
    private CustomIconEntity customIconEntity;

    public NoteCustomIconEntity(UUID noteId, UUID customIconId) {
        this.noteId = noteId;
        this.customIconId = customIconId;
    }

}
