package com.getboostr.portal.database.gift.note;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public interface GiftNoteDetailRepository extends JpaRepository<GiftNoteDetailEntity, UUID> {

}
