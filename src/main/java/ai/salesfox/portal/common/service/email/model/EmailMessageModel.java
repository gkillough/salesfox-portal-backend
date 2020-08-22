package ai.salesfox.portal.common.service.email.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailMessageModel {
    private List<String> recipients;
    private String subjectLine;
    private String messageTitle;
    private String primaryMessage;

}
