package com.usepipeline.portal.database.customization.icon;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomIconOwnerPK implements Serializable {
    private UUID customIconId;
    private UUID userId;

}
