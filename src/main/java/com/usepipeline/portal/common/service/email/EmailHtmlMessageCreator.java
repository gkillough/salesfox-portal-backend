package com.usepipeline.portal.common.service.email;

import com.usepipeline.portal.common.service.email.model.EmailMessageModel;
import org.springframework.stereotype.Component;

@Component
public class EmailHtmlMessageCreator {
    public static final String DEFAULT_EMAIL_TEMPLATE_NAME = "email_default";

    public String createHtmlMessage(String templateName, EmailMessageModel emailMessageModel) {
        StringBuilder htmlBuilder = new StringBuilder();
        htmlBuilder.append("<p>");
        htmlBuilder.append(emailMessageModel.getPrimaryMessage());
        htmlBuilder.append("</p>");

        return htmlBuilder.toString();
    }

}
