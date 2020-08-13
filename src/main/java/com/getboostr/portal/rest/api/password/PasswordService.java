package com.getboostr.portal.rest.api.password;

import com.getboostr.portal.PortalConfiguration;
import com.getboostr.portal.common.service.email.EmailMessagingService;
import com.getboostr.portal.common.service.email.PortalEmailException;
import com.getboostr.portal.common.service.email.model.ButtonEmailMessageModel;
import com.getboostr.portal.common.service.email.model.EmailMessageModel;
import com.getboostr.portal.common.time.PortalDateTimeUtils;
import com.getboostr.portal.database.account.entity.LoginEntity;
import com.getboostr.portal.database.account.entity.PasswordResetTokenEntity;
import com.getboostr.portal.database.account.entity.UserEntity;
import com.getboostr.portal.database.account.key.PasswordResetTokenPK;
import com.getboostr.portal.database.account.repository.LoginRepository;
import com.getboostr.portal.database.account.repository.PasswordResetTokenRepository;
import com.getboostr.portal.database.account.repository.UserRepository;
import com.getboostr.portal.rest.security.authentication.SecurityContextUtils;
import com.getboostr.portal.rest.security.authentication.user.PortalUserDetailsService;
import com.getboostr.portal.rest.security.authorization.PortalAuthorityConstants;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

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
    public PasswordService(PortalConfiguration portalConfiguration, PasswordResetTokenRepository passwordResetTokenRepository,
                           UserRepository userRepository, LoginRepository loginRepository,
                           PortalUserDetailsService userDetailsService, PasswordEncoder passwordEncoder,
                           EmailMessagingService emailMessagingService) {
        this.portalConfiguration = portalConfiguration;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.userRepository = userRepository;
        this.loginRepository = loginRepository;
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
        this.emailMessagingService = emailMessagingService;
    }

    public boolean sendPasswordResetEmail(ResetPasswordModel resetPasswordModel) {
        String email = resetPasswordModel.getEmail();
        if (StringUtils.isBlank(email)) {
            log.error("The field 'email' cannot be blank");
            return false;
        }

        Optional<UserEntity> optionalUser = userRepository.findFirstByEmail(email);
        if (!optionalUser.isPresent()) {
            log.error("No user with that email exists");
            return false;
        }

        String passwordResetToken = UUID.randomUUID().toString();
        PasswordResetTokenEntity passwordResetTokenToSave = new PasswordResetTokenEntity(email, passwordResetToken, PortalDateTimeUtils.getCurrentDateTime());
        passwordResetTokenRepository.save(passwordResetTokenToSave);

        return sendPasswordResetEmail(email, passwordResetToken);
    }

    public boolean validateToken(HttpServletResponse response, String email, String token) {
        if (StringUtils.isBlank(email)) {
            log.error("The parameter 'email' cannot be blank");
            return false;
        }

        if (StringUtils.isBlank(token)) {
            log.error("The parameter 'token' cannot be blank");
            return false;
        }
        PasswordResetTokenPK passwordResetTokenPK = new PasswordResetTokenPK(email, token);
        Optional<OffsetDateTime> optionalTimeGenerated = passwordResetTokenRepository.findById(passwordResetTokenPK)
                .map(PasswordResetTokenEntity::getDateGenerated);

        if (optionalTimeGenerated.isPresent()) {
            Duration timeSinceTokenGenerated = Duration.between(optionalTimeGenerated.get(), PortalDateTimeUtils.getCurrentDateTime());
            if (timeSinceTokenGenerated.compareTo(DURATION_OF_TOKEN_VALIDITY) < 0) {
                grantResetPasswordAuthorityToUser(email);
                response.setHeader("Location", PasswordController.UPDATE_ENDPOINT);
                return true;
            } else {
                log.error("The password reset token has expired");
            }
        } else {
            log.error("No password reset entry for that email/token combination");
        }
        return false;
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

    private boolean sendPasswordResetEmail(String email, String passwordResetToken) {
        String passwordResetUrl = createResetPasswordLink(email, passwordResetToken);

        log.info("*** REMOVE ME *** Password Reset Token: " + passwordResetToken);
        log.info("*** REMOVE ME *** Password Reset URL: " + passwordResetUrl);

        EmailMessageModel emailMessage = createPasswordResetMessageModel(email, passwordResetUrl);
        try {
            emailMessagingService.sendMessage(emailMessage);
            return true;
        } catch (PortalEmailException e) {
            log.error("Problem sending password reset email", e);
        }
        return false;
    }

    private ButtonEmailMessageModel createPasswordResetMessageModel(String recipientEmail, String passwordResetUrl) {
        return new ButtonEmailMessageModel(
                List.of(recipientEmail),
                "BOOSTR - Password Reset",
                "Password Reset",
                "Click the button below to reset your password. If you did not request a password reset, please contact support.",
                "Reset your password",
                passwordResetUrl
        );
    }

    private String createResetPasswordLink(String email, String passwordResetToken) {
        StringBuilder linkBuilder = new StringBuilder(portalConfiguration.getPortalBaseUrl());
        linkBuilder.append(portalConfiguration.getResetPasswordLinkSpec());
        linkBuilder.append("?token=");
        linkBuilder.append(passwordResetToken);
        linkBuilder.append("&email=");
        linkBuilder.append(email);
        return linkBuilder.toString();
    }

}
