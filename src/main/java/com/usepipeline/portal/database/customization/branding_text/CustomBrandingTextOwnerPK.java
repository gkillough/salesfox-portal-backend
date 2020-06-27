package com.usepipeline.portal.database.customization.branding_text;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomBrandingTextOwnerPK implements Serializable {
    private UUID customBrandingTextId;
    private UUID userId;

}
