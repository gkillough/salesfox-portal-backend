package com.usepipeline.portal.database.gift.note;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@IdClass(GiftNoteDetailPK.class)
@Table(schema = "portal", name = "gift_note_details")
public class GiftNoteDetailEntity {
    @Id
    @PrimaryKeyJoinColumn
    @Column(name = "gift_id")
    private UUID giftId;

    @Id
    @PrimaryKeyJoinColumn
    @Column(name = "note_id")
    private UUID noteId;

}
