package com.usepipeline.portal.web.customization.icon.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomIconResponseModel {
    private UUID customIconId;
    private String label;
    private UUID organizationAccountId;
    private UUID ownerId;
    private UUID uploaderId;
    private Boolean isActive;

}
