package com.getboostr.portal.rest.api.note.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NoteResponseModel {
    private UUID noteId;
    private UUID organizationAccountId;
    private UUID updatedByUserId;
    private String message;

}
