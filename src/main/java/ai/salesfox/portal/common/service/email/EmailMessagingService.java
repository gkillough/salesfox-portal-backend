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
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class EmailMessagingService {
    private final EmailConfiguration emailConfiguration;
    private final EmailHtmlMessageCreator emailHtmlMessageCreator;

    @Autowired
    public EmailMessagingService(EmailConfiguration emailConfiguration, EmailHtmlMessageCreator emailHtmlMessageCreator) {
        this.emailConfiguration = emailConfiguration;
        this.emailHtmlMessageCreator = emailHtmlMessageCreator;
    }

    public void sendMessage(EmailMessageModel emailMessageModel) throws SalesfoxEmailException {
        validateEmailConfiguration();
        validateEmailMessageModel(emailMessageModel);
        try {
            Session smtpMailServerSession = Session.getDefaultInstance(emailConfiguration.getSmtpProperties());
            MimeMessage mimeMessage = createMimeMessage(smtpMailServerSession, emailMessageModel);
            sendMessage(smtpMailServerSession, mimeMessage);
        } catch (MessagingException e) {
            log.error("There was an issue sending the email message with the subject line [{}] to  the following recipients: {}", emailMessageModel.getSubjectLine(), emailMessageModel.getRecipients(), e);
            throw new SalesfoxEmailException(e);
        }
    }

    private void validateEmailConfiguration() throws SalesfoxEmailException {
        if (StringUtils.isBlank(emailConfiguration.getSmtpHost())) {
            throw new SalesfoxEmailException("No email server configured");
        }
    }

    private void validateEmailMessageModel(EmailMessageModel emailMessageModel) throws SalesfoxEmailException {
        if (StringUtils.isBlank(emailMessageModel.getSubjectLine())) {
            throw new SalesfoxEmailException("Cannot send an email with a blank subject line");
        }
        if (StringUtils.isBlank(emailMessageModel.getMessageTitle())) {
            throw new SalesfoxEmailException("Cannot send an email with a blank title");
        }
        if (StringUtils.isBlank(emailMessageModel.getPrimaryMessage())) {
            throw new SalesfoxEmailException("Cannot send an email with a blank message");
        }
        if (emailMessageModel.getRecipients() == null || emailMessageModel.getRecipients().isEmpty()) {
            throw new SalesfoxEmailException("Cannot send an email without recipients");
        }
    }

    private MimeMessage createMimeMessage(Session session, EmailMessageModel emailMessageModel) throws SalesfoxEmailException, MessagingException {
        MimeMessage mimeMessage = new MimeMessage(session);
        mimeMessage.setSubject(emailMessageModel.getSubjectLine());
        mimeMessage.setFrom(emailConfiguration.getSmtpFrom());

        Address[] wrappedAddresses = createAddresses(emailMessageModel.getRecipients());
        mimeMessage.setRecipients(Message.RecipientType.TO, wrappedAddresses);

        MimeMultipart mimeMultipart = new MimeMultipart();

        // Message Body
        String htmlMessage = emailHtmlMessageCreator.createHtmlMessage(EmailHtmlMessageCreator.DEFAULT_EMAIL_TEMPLATE_NAME, emailMessageModel);
        BodyPart htmlBodyPart = new MimeBodyPart();
        htmlBodyPart.setContent(htmlMessage, "text/html");
        mimeMultipart.addBodyPart(htmlBodyPart);

        // Image attachment
        BodyPart imageBodyPart = new MimeBodyPart();
        DataSource imageDataSource = new FileDataSource(emailConfiguration.getLogoPng());
        DataHandler imageDataHandler = new DataHandler(imageDataSource);
        imageBodyPart.setDataHandler(imageDataHandler);
        imageBodyPart.setHeader("Content-ID", "<logoImage>");
        mimeMultipart.addBodyPart(imageBodyPart);

        mimeMessage.setContent(mimeMultipart);
        return mimeMessage;
    }

    private Address[] createAddresses(List<String> recipients) throws SalesfoxEmailException, AddressException {
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
            throw new SalesfoxEmailException("No recipient addresses were valid email addresses");
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
