package com.getboostr.portal.rest.api.contact.interaction.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContactInteractionRequestModel {
    private String medium;
    private String classification;
    private OffsetDateTime date;
    private String note;

}
