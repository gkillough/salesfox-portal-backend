package com.usepipeline.portal.database.gift.note;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GiftNoteDetailPK implements Serializable {
    private UUID giftId;
    private UUID noteId;

}
