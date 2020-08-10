package com.getboostr.portal.rest.api.customization.icon.model;

import com.getboostr.portal.rest.api.common.model.request.RestrictionModel;
import com.getboostr.portal.rest.api.user.common.model.UserSummaryModel;
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
    private UserSummaryModel uploader;
    private Boolean isActive;
    private RestrictionModel restriction;

}
