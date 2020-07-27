package com.getboostr.portal.rest.api.contact.interaction.model;

import com.getboostr.portal.common.model.PortalDateModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContactInteractionRequestModel {
    private UUID interactingUserId;
    private String medium;
    private String classification;
    private PortalDateModel date;
    private String note;

}
