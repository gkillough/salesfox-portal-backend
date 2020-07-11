package com.usepipeline.portal.common.service.email;

import com.usepipeline.portal.common.service.email.model.EmailMessageModel;
import freemarker.template.Configuration;
import org.junit.jupiter.api.Test;

import java.util.List;

public class EmailMessageCreatorTest {
    @Test
    public void createHtmlMessageTest() throws Exception {
        FreemarkerConfiguration freemarkerConfiguration = new FreemarkerConfiguration();
        Configuration freemarkerConfig = freemarkerConfiguration.createFreemarkerConfig();
        EmailHtmlMessageCreator emailHtmlMessageCreator = new EmailHtmlMessageCreator(freemarkerConfig);

        EmailMessageModel emailMessageModel = new EmailMessageModel(List.of(), "Subject Line", "Message Title", "Primary Message");
        String htmlMessage = emailHtmlMessageCreator.createHtmlMessage(EmailHtmlMessageCreator.DEFAULT_EMAIL_TEMPLATE_NAME, emailMessageModel);
        System.out.println(htmlMessage);
    }

}
