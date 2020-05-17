package com.usepipeline.portal.common.service.email;

import com.usepipeline.portal.common.service.email.model.EmailMessageModel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.mail.*;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class EmailMessagingService {
    private EmailConfiguration emailConfiguration;

    @Autowired
    public EmailMessagingService(EmailConfiguration emailConfiguration) {
        this.emailConfiguration = emailConfiguration;
    }

    public void sendMessage(EmailMessageModel emailMessageModel) throws PortalEmailException {
        validateEmailConfiguration();
        validateEmailMessageModel(emailMessageModel);
        try {
            Session smtpMailServerSession = Session.getDefaultInstance(emailConfiguration.getSmtpProperties());
            MimeMessage mimeMessage = createMimeMessage(smtpMailServerSession, emailMessageModel);
            sendMessage(smtpMailServerSession, mimeMessage);
        } catch (MessagingException e) {
            log.error("There was an issue sending the email message with the subject line [{}] to  the following recipients: {}", emailMessageModel.getSubjectLine(), emailMessageModel.getRecipients(), e);
            throw new PortalEmailException(e);
        }
    }

    private void validateEmailConfiguration() throws PortalEmailException {
        if (StringUtils.isBlank(emailConfiguration.getSmtpHost())) {
            throw new PortalEmailException("No email server configured");
        }
    }

    private void validateEmailMessageModel(EmailMessageModel emailMessageModel) throws PortalEmailException {
        if (StringUtils.isBlank(emailMessageModel.getSubjectLine())) {
            throw new PortalEmailException("Cannot send an email with a blank subject line");
        }
        if (StringUtils.isBlank(emailMessageModel.getPrimaryMessage())) {
            throw new PortalEmailException("Cannot send an email with a blank message");
        }
        if (emailMessageModel.getRecipients() == null || emailMessageModel.getRecipients().isEmpty()) {
            throw new PortalEmailException("Cannot send an email without recipients");
        }
    }

    private MimeMessage createMimeMessage(Session session, EmailMessageModel emailMessageModel) throws PortalEmailException, MessagingException {
        MimeMessage mimeMessage = new MimeMessage(session);
        mimeMessage.setSubject(emailMessageModel.getSubjectLine());
        mimeMessage.setFrom(emailConfiguration.getSmtpFrom());

        String htmlMessage = createHtmlMessage(emailMessageModel);
        mimeMessage.setContent(htmlMessage, "text/html");

        Address[] wrappedAddresses = createAddresses(emailMessageModel.getRecipients());
        mimeMessage.setRecipients(Message.RecipientType.TO, wrappedAddresses);

        return mimeMessage;
    }

    // TODO abstract this into its own utility when full build-out is completed
    private String createHtmlMessage(EmailMessageModel emailMessageModel) {
        StringBuilder htmlBuilder = new StringBuilder();
        htmlBuilder.append("<p>");
        htmlBuilder.append(emailMessageModel.getPrimaryMessage());
        htmlBuilder.append("</p>");

        return htmlBuilder.toString();
    }

    private Address[] createAddresses(List<String> recipients) throws PortalEmailException, AddressException {
        List<Address> recipientEmailAddresses = new ArrayList<>();
        for (String recipientEmailAddressString : recipients) {
            try {
                InternetAddress asAddress = new InternetAddress(recipientEmailAddressString);
                asAddress.validate();
                recipientEmailAddresses.add(asAddress);
            } catch (AddressException e) {
                if (!emailConfiguration.getSmtpSendPartial()) {
                    throw e;
                } else {
                    log.warn("The recipient email address [{}] was in an invalid format: {}", recipientEmailAddressString, e.getMessage());
                }
            }
        }

        if (recipientEmailAddresses.isEmpty()) {
            throw new PortalEmailException("No recipient addresses were valid email addresses");
        }

        Address[] addressesArray = new Address[recipientEmailAddresses.size()];
        return recipientEmailAddresses.toArray(addressesArray);
    }

    private void sendMessage(Session session, MimeMessage mimeMessage) throws MessagingException {
        log.info("Attempting to send an email...");
        try (Transport transport = session.getTransport("smtp")) {
            if (emailConfiguration.getSmtpAuth()) {
                log.info("Authenticating with email server...");
                transport.connect(emailConfiguration.getSmtpHost(), emailConfiguration.getSmtpPort(), emailConfiguration.getSmtpUser(), emailConfiguration.getSmtpPassword());
            }
            transport.sendMessage(mimeMessage, mimeMessage.getAllRecipients());
            log.info("Successfully sent an email!");
        }
    }

}
