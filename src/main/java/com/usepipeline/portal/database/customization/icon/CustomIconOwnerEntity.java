package com.usepipeline.portal.database.customization.icon;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(schema = "portal", name = "custom_icon_owners")
public class CustomIconOwnerEntity implements Serializable {
    @Id
    @Column(name = "custom_icon_id")
    private UUID customIconId;

    @Column(name = "user_id")
    private UUID userId;

}
