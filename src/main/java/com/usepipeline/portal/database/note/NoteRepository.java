package com.usepipeline.portal.database.note;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public interface NoteRepository extends JpaRepository<NoteEntity, UUID> {

}
