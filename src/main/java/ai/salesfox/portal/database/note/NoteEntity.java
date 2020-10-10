package ai.salesfox.portal.database.note;

import ai.salesfox.portal.database.account.entity.UserEntity;
import ai.salesfox.portal.database.note.restriction.NoteOrganizationAccountRestrictionEntity;
import ai.salesfox.portal.database.note.restriction.NoteUserRestrictionEntity;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@Entity
@Table(schema = "portal", name = "notes")
public class NoteEntity implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "note_id")
    private UUID noteId;

    @PrimaryKeyJoinColumn
    @Column(name = "updated_by_user_id")
    private UUID updatedByUserId;

    @Column(name = "date_modified")
    private OffsetDateTime dateModified;

    @Column(name = "message")
    private String message;

    @Column(name = "font_size")
    private String fontSize;

    @Column(name = "font_color")
    private String fontColor;

    @Column(name = "handwriting_style")
    private String handwritingStyle;

    @OneToOne
    @JoinColumn(name = "note_id", referencedColumnName = "note_id", insertable = false, updatable = false)
    private NoteOrganizationAccountRestrictionEntity noteOrganizationAccountRestrictionEntity;

    @OneToOne
    @JoinColumn(name = "note_id", referencedColumnName = "note_id", insertable = false, updatable = false)
    private NoteUserRestrictionEntity noteUserRestrictionEntity;

    @ManyToOne
    @JoinColumn(name = "updated_by_user_id", referencedColumnName = "user_id", insertable = false, updatable = false)
    private UserEntity updatedByUserEntity;

    public NoteEntity(UUID noteId, UUID updatedByUserId, OffsetDateTime dateModified, String message, String fontSize, String fontColor, String handwritingStyle) {
        this.noteId = noteId;
        this.updatedByUserId = updatedByUserId;
        this.dateModified = dateModified;
        this.message = message;
        this.fontSize = fontSize;
        this.fontColor = fontColor;
        this.handwritingStyle = handwritingStyle;
    }

}
