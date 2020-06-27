package com.usepipeline.portal.database.gift.note;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

@Component
public interface GiftNoteDetailRepository extends JpaRepository<GiftNoteDetailEntity, GiftNoteDetailPK> {

}
