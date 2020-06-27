package com.usepipeline.portal.database.customization.icon;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@IdClass(CustomIconOwnerPK.class)
@Table(schema = "portal", name = "custom_icon_owners")
public class CustomIconOwnerEntity implements Serializable {
    @Id
    @Column(name = "custom_icon_id")
    private UUID customIconId;

    @Id
    @Column(name = "user_id")
    private UUID userId;

}
