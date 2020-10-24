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
import ai.salesfox.portal.rest.security.authentication.SecurityContextUtils;
import ai.salesfox.portal.rest.security.authentication.user.PortalUserDetailsService;
import ai.salesfox.portal.rest.security.authorization.PortalAuthorityConstants;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
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
    private final PortalUserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final EmailMessagingService emailMessagingService;

    @Autowired
    public PasswordService(
            PortalConfiguration portalConfiguration,
            PasswordResetTokenRepository passwordResetTokenRepository,
            UserRepository userRepository,
            LoginRepository loginRepository,
            PortalUserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder,
            EmailMessagingService emailMessagingService
    ) {
        this.portalConfiguration = portalConfiguration;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.userRepository = userRepository;
        this.loginRepository = loginRepository;
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
        this.emailMessagingService = emailMessagingService;
    }

    public void sendPasswordResetEmail(ResetPasswordModel resetPasswordModel) {
        String email = resetPasswordModel.getEmail();
        if (StringUtils.isBlank(email)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The field 'email' cannot be blank");
        }

        Optional<UserEntity> optionalUser = userRepository.findFirstByEmail(email);
        if (!optionalUser.isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No user with that email exists");
        }

        String passwordResetToken = UUID.randomUUID().toString();
        PasswordResetTokenEntity passwordResetTokenToSave = new PasswordResetTokenEntity(email, passwordResetToken, PortalDateTimeUtils.getCurrentDateTime());
        passwordResetTokenRepository.save(passwordResetTokenToSave);

        sendPasswordResetEmail(email, passwordResetToken);
    }

    public void validateToken(HttpServletResponse response, String email, String token) {
        if (StringUtils.isBlank(email)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The parameter 'email' cannot be blank");
        }

        if (StringUtils.isBlank(token)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The parameter 'token' cannot be blank");
        }
        PasswordResetTokenPK passwordResetTokenPK = new PasswordResetTokenPK(email, token);
        Optional<OffsetDateTime> optionalTimeGenerated = passwordResetTokenRepository.findById(passwordResetTokenPK)
                .map(PasswordResetTokenEntity::getDateGenerated);

        if (optionalTimeGenerated.isPresent()) {
            Duration timeSinceTokenGenerated = Duration.between(optionalTimeGenerated.get(), PortalDateTimeUtils.getCurrentDateTime());
            if (timeSinceTokenGenerated.compareTo(DURATION_OF_TOKEN_VALIDITY) < 0) {
                grantResetPasswordAuthorityToUser(email);

                String frontEndLocation = String.format("%s%s", portalConfiguration.getPortalFrontEndUrl(), portalConfiguration.getFrontEndResetPasswordRoute());
                response.setHeader("Location", frontEndLocation);
            } else {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The password reset token has expired");
            }
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No password reset entry for that email/token combination");
        }
    }

    @Transactional
    public boolean updateAuthenticatedUserPassword(HttpServletResponse response, UpdatePasswordModel updatePasswordModel) {
        Optional<UsernamePasswordAuthenticationToken> optionalUserAuthToken = SecurityContextUtils.retrieveUserAuthToken();
        if (optionalUserAuthToken.isPresent()) {
            UsernamePasswordAuthenticationToken userAuthToken = optionalUserAuthToken.get();
            UserDetails userDetails = SecurityContextUtils.extractUserDetails(userAuthToken);

            String authenticatedUserEmail = userDetails.getUsername();
            boolean wasSaveSuccessful = updatePassword(authenticatedUserEmail, updatePasswordModel.getNewPassword());
            if (wasSaveSuccessful) {
                response.setHeader("Location", "/");
                clearResetPasswordAuthorityFromSecurityContext();
                return true;
            }
        }
        log.error("The password could not be updated");
        return false;
    }

    @Transactional
    public boolean updatePassword(String email, String newPassword) {
        if (StringUtils.isBlank(email) || StringUtils.isBlank(newPassword)) {
            log.error("Blank credential(s) provided");
            return false;
        }
        return persistPasswordUpdate(email, newPassword);
    }

    private boolean persistPasswordUpdate(String email, String password) {
        Optional<LoginEntity> optionalLoginEntity = userRepository.findFirstByEmail(email)
                .map(UserEntity::getUserId)
                .map(loginRepository::findById)
                .filter(Optional::isPresent)
                .map(Optional::get);
        if (optionalLoginEntity.isPresent()) {
            String encodedPassword = passwordEncoder.encode(password);

            LoginEntity loginEntity = optionalLoginEntity.get();
            loginEntity.setPasswordHash(encodedPassword);
            loginEntity.setNumFailedLogins(0);
            loginRepository.save(loginEntity);

            // Once the password has been reset, all tokens for that user can be cleared.
            passwordResetTokenRepository.deleteByEmail(email);
            return true;
        }
        log.error("No login found");
        return false;
    }

    private void grantResetPasswordAuthorityToUser(String email) {
        UserDetails user = userDetailsService.loadUserByUsername(email);

        Authentication auth = new UsernamePasswordAuthenticationToken(
                user, null, List.of(new SimpleGrantedAuthority(PortalAuthorityConstants.UPDATE_PASSWORD_PERMISSION)));
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    private void clearResetPasswordAuthorityFromSecurityContext() {
        SecurityContextHolder.clearContext();
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
        StringBuilder linkBuilder = new StringBuilder(portalConfiguration.getPortalBackEndUrl());
        linkBuilder.append(PasswordController.GRANT_UPDATE_PERMISSION_ENDPOINT);
        linkBuilder.append("?token=");
        linkBuilder.append(passwordResetToken);
        linkBuilder.append("&email=");
        linkBuilder.append(email);
        return linkBuilder.toString();
    }

}
