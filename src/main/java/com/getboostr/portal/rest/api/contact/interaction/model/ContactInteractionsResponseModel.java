package com.getboostr.portal.rest.api.contact.interaction.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContactInteractionsResponseModel {
    private UUID interactionId;
    private String medium;
    private String classification;
    private OffsetDateTime date;
    private String note;

}
