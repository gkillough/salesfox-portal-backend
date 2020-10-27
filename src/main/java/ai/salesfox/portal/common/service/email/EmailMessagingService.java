package ai.salesfox.portal.common.service.email;

import ai.salesfox.portal.common.service.email.model.EmailMessageModel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
public class EmailMessagingService {
    public static final String ATTACHMENT_HEADER_CONTENT_ID = "Content-ID";
    private final EmailConfiguration emailConfiguration;
    private final EmailHtmlMessageCreator emailHtmlMessageCreator;

    @Autowired
    public EmailMessagingService(EmailConfiguration emailConfiguration, EmailHtmlMessageCreator emailHtmlMessageCreator) {
        this.emailConfiguration = emailConfiguration;
        this.emailHtmlMessageCreator = emailHtmlMessageCreator;
    }

    public void sendMessage(EmailMessageModel emailMessageModel) throws PortalEmailException {
        sendMessage(emailMessageModel, List.of());
    }

    public void sendMessage(EmailMessageModel emailMessageModel, Collection<File> attachments) throws PortalEmailException {
        validateEmailConfiguration();
        validateEmailMessageModel(emailMessageModel);
        try {
            Session smtpMailServerSession = Session.getDefaultInstance(emailConfiguration.getSmtpProperties());
            MimeMessage mimeMessage = createMimeMessage(smtpMailServerSession, emailMessageModel, attachments);
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
        if (StringUtils.isBlank(emailMessageModel.getMessageTitle())) {
            throw new PortalEmailException("Cannot send an email with a blank title");
        }
        if (StringUtils.isBlank(emailMessageModel.getPrimaryMessage())) {
            throw new PortalEmailException("Cannot send an email with a blank message");
        }
        if (emailMessageModel.getRecipients() == null || emailMessageModel.getRecipients().isEmpty()) {
            throw new PortalEmailException("Cannot send an email without recipients");
        }
    }

    private MimeMessage createMimeMessage(Session session, EmailMessageModel emailMessageModel, Collection<File> attachments) throws PortalEmailException, MessagingException {
        MimeMessage mimeMessage = new MimeMessage(session);
        mimeMessage.setSubject(emailMessageModel.getSubjectLine());
        mimeMessage.setFrom(emailConfiguration.getSmtpFrom());

        Address[] wrappedAddresses = createAddresses(emailMessageModel.getRecipients());
        mimeMessage.setRecipients(Message.RecipientType.TO, wrappedAddresses);

        MimeMultipart mimeMultipart = new MimeMultipart();

        // Message Body
        String templateFile = Optional.ofNullable(emailMessageModel.getTemplateFileName()).orElse(EmailHtmlMessageCreator.DEFAULT_EMAIL_TEMPLATE_NAME);
        String htmlMessage = emailHtmlMessageCreator.createHtmlMessage(templateFile, emailMessageModel);
        BodyPart htmlBodyPart = new MimeBodyPart();
        htmlBodyPart.setContent(htmlMessage, "text/html");
        mimeMultipart.addBodyPart(htmlBodyPart);

        // Image attachment
        BodyPart imageBodyPart = createAttachmentBodyPart(emailConfiguration.getLogoPng());
        imageBodyPart.setHeader(ATTACHMENT_HEADER_CONTENT_ID, "<logoImage>");
        mimeMultipart.addBodyPart(imageBodyPart);

        for (File attachment : attachments) {
            BodyPart attachmentBodyPart = createAttachmentBodyPart(attachment);
            mimeMultipart.addBodyPart(attachmentBodyPart);
        }

        mimeMessage.setContent(mimeMultipart);
        return mimeMessage;
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

    private BodyPart createAttachmentBodyPart(File attachmentSource) throws MessagingException {
        BodyPart attachmentBodyPart = new MimeBodyPart();
        DataSource dataSource = new FileDataSource(attachmentSource);
        DataHandler dataHandler = new DataHandler(dataSource);
        attachmentBodyPart.setDataHandler(dataHandler);
        return attachmentBodyPart;
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
