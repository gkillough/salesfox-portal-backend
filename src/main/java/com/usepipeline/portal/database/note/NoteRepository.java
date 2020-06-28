package com.usepipeline.portal.database.note;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public interface NoteRepository extends JpaRepository<NoteEntity, UUID> {
    Page<NoteEntity> findAllByUpdatedByUserId(UUID updatedByUserId, Pageable pageable);

    Page<NoteEntity> findAllByOrganizationAccountId(UUID updatedByUserId, Pageable pageable);

}
