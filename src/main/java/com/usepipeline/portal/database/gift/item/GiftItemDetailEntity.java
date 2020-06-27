package com.usepipeline.portal.database.gift.item;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@IdClass(GiftItemDetailPK.class)
@Table(schema = "portal", name = "gift_item_details")
public class GiftItemDetailEntity {
    @Id
    @PrimaryKeyJoinColumn
    @Column(name = "gift_id")
    private UUID giftId;

    @Id
    @PrimaryKeyJoinColumn
    @Column(name = "item_id")
    private UUID itemId;

}
