package com.getboostr.portal.rest.api.note.model;

import com.getboostr.portal.rest.api.common.model.request.RestrictionModel;
import com.getboostr.portal.rest.api.user.common.model.UserSummaryModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NoteResponseModel {
    private UUID noteId;
    private String message;
    private OffsetDateTime dateModified;
    private UserSummaryModel updatedByUser;
    private RestrictionModel restriction;

}
