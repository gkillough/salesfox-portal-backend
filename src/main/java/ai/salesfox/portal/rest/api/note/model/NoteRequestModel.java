package ai.salesfox.portal.rest.api.note.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NoteRequestModel {
    private String message;
    private String fontSize;
    private String fontColor;
    private String handwritingStyle;
    private UUID headerCustomIconId;

}
