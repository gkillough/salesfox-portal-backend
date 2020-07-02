package com.usepipeline.portal.database.note.request.status;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(schema = "portal", name = "note_request_statuses")
@Deprecated
public class NoteRequestStatusEntity implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "status_id")
    private UUID statusId;

    @PrimaryKeyJoinColumn
    @Column(name = "request_id")
    private UUID requestId;

    @PrimaryKeyJoinColumn
    @Column(name = "changed_by_user_id")
    private UUID changedByUserId;

    @Column(name = "status")
    private String status;

    @Column(name = "date_submitted")
    private OffsetDateTime dateSubmitted;

    @Column(name = "date_updated")
    private OffsetDateTime dateUpdated;

}
