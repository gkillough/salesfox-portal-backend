package ai.salesfox.portal.rest.api.note.model;

import ai.salesfox.portal.rest.api.common.model.request.RestrictionModel;
import ai.salesfox.portal.rest.api.user.common.model.UserSummaryModel;
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
    private Integer fontSize;
    private String fontColor;
    private String handwritingStyle;
    private OffsetDateTime dateModified;
    private UserSummaryModel updatedByUser;
    private RestrictionModel restriction;

}
