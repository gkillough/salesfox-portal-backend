package com.usepipeline.portal.database.note.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(schema = "portal", name = "note_requests")
@Deprecated
public class NoteRequestEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "request_id")
    private UUID requestId;

    @PrimaryKeyJoinColumn
    @Column(name = "note_id")
    private UUID noteId;

    @PrimaryKeyJoinColumn
    @Column(name = "contact_id")
    private UUID contactId;

    @PrimaryKeyJoinColumn
    @Column(name = "requesting_user_id")
    private UUID requestingUserId;

}
