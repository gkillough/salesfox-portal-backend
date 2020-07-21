package com.getboostr.portal.common.service.email;

import com.getboostr.portal.common.service.email.model.EmailMessageModel;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.StringWriter;

@Component
public class EmailHtmlMessageCreator {
    public static final String DEFAULT_EMAIL_TEMPLATE_NAME = "email_default.ftl";

    private Configuration freemarkerConfig;

    @Autowired
    public EmailHtmlMessageCreator(Configuration freemarkerConfig) {
        this.freemarkerConfig = freemarkerConfig;
    }

    public String createHtmlMessage(String templateName, EmailMessageModel emailMessageModel) throws PortalEmailException {
        try {
            Template template = freemarkerConfig.getTemplate(templateName);
            StringWriter stringWriter = new StringWriter();
            template.process(emailMessageModel, stringWriter);
            return stringWriter.toString();
        } catch (IOException | TemplateException e) {
            throw new PortalEmailException(e.getMessage(), e);
        }
    }

}
