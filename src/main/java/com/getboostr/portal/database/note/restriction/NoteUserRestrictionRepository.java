package com.getboostr.portal.database.note.restriction;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public interface NoteUserRestrictionRepository extends JpaRepository<NoteUserRestrictionEntity, UUID> {
}
