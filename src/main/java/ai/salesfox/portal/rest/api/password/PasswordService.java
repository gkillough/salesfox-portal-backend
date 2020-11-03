package ai.salesfox.portal.rest.api.password;

import ai.salesfox.portal.PortalConfiguration;
import ai.salesfox.portal.common.service.email.EmailMessagingService;
import ai.salesfox.portal.common.service.email.PortalEmailException;
import ai.salesfox.portal.common.service.email.model.ButtonEmailMessageModel;
import ai.salesfox.portal.common.service.email.model.EmailMessageModel;
import ai.salesfox.portal.common.time.PortalDateTimeUtils;
import ai.salesfox.portal.database.account.entity.LoginEntity;
import ai.salesfox.portal.database.account.entity.PasswordResetTokenEntity;
import ai.salesfox.portal.database.account.entity.UserEntity;
import ai.salesfox.portal.database.account.key.PasswordResetTokenPK;
import ai.salesfox.portal.database.account.repository.LoginRepository;
import ai.salesfox.portal.database.account.repository.PasswordResetTokenRepository;
import ai.salesfox.portal.database.account.repository.UserRepository;
import ai.salesfox.portal.rest.api.common.model.response.ValidationResponseModel;
import ai.salesfox.portal.rest.api.password.model.ResetPasswordModel;
import ai.salesfox.portal.rest.api.password.model.UpdatePasswordModel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@Slf4j
public class PasswordService {
    public static final Duration DURATION_OF_TOKEN_VALIDITY = Duration.ofMinutes(30);

    private final PortalConfiguration portalConfiguration;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final UserRepository userRepository;
    private final LoginRepository loginRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailMessagingService emailMessagingService;

    @Autowired
    public PasswordService(
            PortalConfiguration portalConfiguration,
            PasswordResetTokenRepository passwordResetTokenRepository,
            UserRepository userRepository,
            LoginRepository loginRepository,
            PasswordEncoder passwordEncoder,
            EmailMessagingService emailMessagingService
    ) {
        this.portalConfiguration = portalConfiguration;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.userRepository = userRepository;
        this.loginRepository = loginRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailMessagingService = emailMessagingService;
    }

    @Transactional
    public void sendPasswordResetEmail(ResetPasswordModel resetPasswordModel) {
        String email = resetPasswordModel.getEmail();
        if (StringUtils.isBlank(email)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The field 'email' cannot be blank");
        }

        Optional<UserEntity> optionalUser = userRepository.findFirstByEmail(email);
        if (optionalUser.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No user with that email exists");
        }

        String passwordResetToken = UUID.randomUUID().toString();
        PasswordResetTokenEntity passwordResetTokenToSave = new PasswordResetTokenEntity(email, passwordResetToken, PortalDateTimeUtils.getCurrentDateTime());
        passwordResetTokenRepository.save(passwordResetTokenToSave);

        sendPasswordResetEmail(email, passwordResetToken);
    }

    @Transactional(readOnly = true)
    public ValidationResponseModel validateToken(String email, String token) {
        Duration timeSinceTokenGenerated = validateAndRetrieveDurationSinceGenerated(email, token);
        if (timeSinceTokenGenerated.compareTo(DURATION_OF_TOKEN_VALIDITY) < 0) {
            Duration remainingTokenValidity = DURATION_OF_TOKEN_VALIDITY.minus(timeSinceTokenGenerated);
            return new ValidationResponseModel(true, String.format("The token expires in %d minutes", remainingTokenValidity.toMinutes()));
        } else {
            return new ValidationResponseModel(false, "The password reset token has expired");
        }
    }

    @Transactional
    public void updatePasswordWithTokenAndEmail(UpdatePasswordModel updatePasswordModel) {
        String email = updatePasswordModel.getEmail();
        Duration timeSinceTokenGenerated = validateAndRetrieveDurationSinceGenerated(email, updatePasswordModel.getToken());
        if (timeSinceTokenGenerated.compareTo(DURATION_OF_TOKEN_VALIDITY) < 0) {
            updatePassword(email, updatePasswordModel.getNewPassword());

            // Once the password has been reset, all tokens for that user can be cleared.
            passwordResetTokenRepository.deleteByEmail(email);
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The password reset token has expired");
        }
    }

    @Transactional
    public void updatePassword(String email, String newPassword) {
        if (StringUtils.isBlank(email) || StringUtils.isBlank(newPassword)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Blank credential(s) provided");
        }
        persistPasswordUpdate(email, newPassword);
    }

    private void persistPasswordUpdate(String email, String password) {
        Optional<LoginEntity> optionalLoginEntity = userRepository.findFirstByEmail(email)
                .map(UserEntity::getLoginEntity);
        if (optionalLoginEntity.isPresent()) {
            String encodedPassword = passwordEncoder.encode(password);

            LoginEntity loginEntity = optionalLoginEntity.get();
            loginEntity.setPasswordHash(encodedPassword);
            loginEntity.setNumFailedLogins(0);
            loginRepository.save(loginEntity);
        } else {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "No login info found for user");
        }
    }

    private Duration validateAndRetrieveDurationSinceGenerated(String email, String token) {
        if (StringUtils.isBlank(email)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The parameter 'email' cannot be blank");
        }

        if (StringUtils.isBlank(token)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The parameter 'token' cannot be blank");
        }

        PasswordResetTokenPK passwordResetTokenPK = new PasswordResetTokenPK(email, token);
        OffsetDateTime tokenTimeGenerated = passwordResetTokenRepository.findById(passwordResetTokenPK)
                .map(PasswordResetTokenEntity::getDateGenerated)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "No password reset entry for that email/token combination"));
        return Duration.between(tokenTimeGenerated, PortalDateTimeUtils.getCurrentDateTime());
    }

    private void sendPasswordResetEmail(String email, String passwordResetToken) {
        String passwordResetUrl = createResetPasswordLink(email, passwordResetToken);

        log.info("*** REMOVE ME *** Password Reset URL: {}", passwordResetUrl);
        EmailMessageModel emailMessage = createPasswordResetMessageModel(email, passwordResetUrl);
        try {
            emailMessagingService.sendMessage(emailMessage);
        } catch (PortalEmailException e) {
            log.error("Problem sending password reset email", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "There was a problem sending the email");
        }
    }

    private ButtonEmailMessageModel createPasswordResetMessageModel(String recipientEmail, String passwordResetUrl) {
        return new ButtonEmailMessageModel(
                List.of(recipientEmail),
                "Salesfox - Password Reset",
                "Password Reset",
                "Click the button below to reset your password. If you did not request a password reset, please contact support.",
                "Reset your password",
                passwordResetUrl
        );
    }

    private String createResetPasswordLink(String email, String passwordResetToken) {
        StringBuilder linkBuilder = new StringBuilder(portalConfiguration.getPortalFrontEndUrl());
        linkBuilder.append(portalConfiguration.getFrontEndResetPasswordRoute());
        linkBuilder.append("?token=");
        linkBuilder.append(passwordResetToken);
        linkBuilder.append("&email=");
        linkBuilder.append(email);
        return linkBuilder.toString();
    }

}
