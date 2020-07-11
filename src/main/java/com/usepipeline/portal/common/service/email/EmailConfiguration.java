package com.usepipeline.portal.common.service.email;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.io.File;
import java.util.Properties;

@Data
@Configuration
@PropertySource(EmailConfiguration.EMAIL_CONFIGURATION_FILE_NAME)
public class EmailConfiguration {
    public static final String EMAIL_CONFIGURATION_FILE_NAME = "email_service.properties";

    @Value("${mail.smtp.host:}")
    private String smtpHost;

    @Value("${mail.smtp.port:25}")
    private Integer smtpPort;

    @Value("${mail.smtp.ssl.enable:true}")
    private Boolean smtpSslEnable;

    @Value("${mail.smtp.auth:true}")
    private Boolean smtpAuth;

    @Value("${mail.smtp.user:}")
    private String smtpUser;

    @Value("${mail.smtp.password:}")
    private String smtpPassword;

    @Value("${mail.smtp.from:noreply@usepipeline.com}")
    private String smtpFrom;

    @Value("${mail.smtp.sendpartial:true}")
    private Boolean smtpSendPartial;

    @Value("${mail.smtp.timeout:300000}")
    private Integer smtpTimeout;

    @Value("${mail.smtp.connectiontimeout:300000}")
    private Integer smtpConnectionTimeout;

    @Value("classpath:/images/boostr_logo.png")
    private File logoPng;

    @Value("classpath:/images/boostr_logo.svg")
    private File logoSvg;

    public Properties getSmtpProperties() {
        Properties smtpProperties = new Properties();
        smtpProperties.put("mail.smtp.host", smtpHost);
        smtpProperties.put("mail.smtp.port", smtpPort);
        smtpProperties.put("mail.smtp.ssl.enable", smtpSslEnable);

        smtpProperties.put("mail.smtp.auth", smtpAuth);
        smtpProperties.put("mail.smtp.user", smtpUser);
        smtpProperties.put("mail.smtp.password", smtpPassword);

        smtpProperties.put("mail.smtp.from", smtpFrom);
        smtpProperties.put("mail.smtp.sendpartial", smtpSendPartial);
        smtpProperties.put("mail.smtp.timeout", smtpTimeout);
        smtpProperties.put("mail.smtp.connectiontimeout", smtpConnectionTimeout);
        return smtpProperties;
    }

}
