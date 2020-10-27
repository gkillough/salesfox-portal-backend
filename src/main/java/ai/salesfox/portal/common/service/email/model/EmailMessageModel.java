package ai.salesfox.portal.common.service.email.model;

import ai.salesfox.portal.common.service.email.EmailHtmlMessageCreator;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class EmailMessageModel {
    private List<String> recipients;
    private String subjectLine;
    private String messageTitle;
    private String primaryMessage;
    private String templateFileName = EmailHtmlMessageCreator.DEFAULT_EMAIL_TEMPLATE_NAME;

    public EmailMessageModel(List<String> recipients, String subjectLine, String messageTitle, String primaryMessage) {
        this.recipients = recipients;
        this.subjectLine = subjectLine;
        this.messageTitle = messageTitle;
        this.primaryMessage = primaryMessage;
    }

}
