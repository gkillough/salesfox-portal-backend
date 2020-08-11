package com.getboostr.portal.rest.api.contact.interaction.model;

import com.getboostr.portal.rest.api.user.common.model.UserSummaryModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContactInteractionsResponseModel {
    private UUID interactionId;
    private UserSummaryModel interactingUser;
    private String medium;
    private String classification;
    private LocalDate date;
    private String note;

}
