package com.getboostr.portal.web.customization.branding_text.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomBrandingTextResponseModel {
    private UUID customBrandingTextId;
    private UUID organizationAccountId;
    private UUID ownerId;
    private UUID uploaderId;
    private String customBrandingText;
    private Boolean isActive;

}
