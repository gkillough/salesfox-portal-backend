package com.getboostr.portal.database.note;

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
@Table(schema = "portal", name = "notes")
public class NoteEntity implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "note_id")
    private UUID noteId;

    @PrimaryKeyJoinColumn
    @Column(name = "organization_account_id")
    private UUID organizationAccountId;

    @PrimaryKeyJoinColumn
    @Column(name = "updated_by_user_id")
    private UUID updatedByUserId;

    @Column(name = "message")
    private String message;

    // TODO add date_updated column

}
