package com.getboostr.portal.database.note.request.status;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Deprecated
public interface NoteRequestStatusRepository extends JpaRepository<NoteRequestStatusEntity, UUID> {

}
