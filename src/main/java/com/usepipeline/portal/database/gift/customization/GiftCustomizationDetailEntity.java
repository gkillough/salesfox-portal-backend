package com.usepipeline.portal.database.gift.customization;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(schema = "portal", name = "gift_customization_details")
public class GiftCustomizationDetailEntity {
    @Id
    @PrimaryKeyJoinColumn
    @Column(name = "gift_id")
    private UUID giftId;

    // TODO If gift to item is many to many, there should be customization details per item.
    //  That means this will need an item_id as part of its PK.

    @PrimaryKeyJoinColumn
    @Column(name = "custom_icon_id")
    private UUID customIconId;

    @PrimaryKeyJoinColumn
    @Column(name = "custom_text_id")
    private UUID customTextId;

}
