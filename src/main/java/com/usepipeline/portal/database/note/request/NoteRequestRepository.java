package com.usepipeline.portal.database.note.request;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public interface NoteRequestRepository extends JpaRepository<NoteRequestEntity, UUID> {

}
