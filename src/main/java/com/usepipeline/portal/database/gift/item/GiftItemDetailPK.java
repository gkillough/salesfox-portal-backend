package com.usepipeline.portal.database.gift.item;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GiftItemDetailPK implements Serializable {
    private UUID giftId;
    private UUID itemId;

}
