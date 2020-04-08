package com.usepipeline.portal.web.password;

import com.usepipeline.portal.common.service.email.EmailMessage;
import com.usepipeline.portal.common.service.email.EmailMessagingService;
import com.usepipeline.portal.common.service.email.PortalEmailException;
import com.usepipeline.portal.database.account.entity.LoginEntity;
import com.usepipeline.portal.database.account.entity.PasswordResetTokenEntity;
import com.usepipeline.portal.database.account.entity.UserEntity;
import com.usepipeline.portal.database.account.key.PasswordResetTokenPK;
import com.usepipeline.portal.database.account.repository.LoginRepository;
import com.usepipeline.portal.database.account.repository.PasswordResetTokenRepository;
import com.usepipeline.portal.database.account.repository.UserRepository;
import com.usepipeline.portal.web.security.authentication.SecurityContextUtils;
import com.usepipeline.portal.web.security.authentication.user.PortalUserDetailsService;
import com.usepipeline.portal.web.security.authorization.PortalAuthorityConstants;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

@Component
@Slf4j
public class PasswordService {
    public static final Duration DURATION_OF_TOKEN_VALIDITY = Duration.ofMinutes(30);

    private PasswordResetTokenRepository passwordResetTokenRepository;
    private UserRepository userRepository;
    private LoginRepository loginRepository;
    private PortalUserDetailsService userDetailsService;
    private PasswordEncoder passwordEncoder;
    private EmailMessagingService emailMessagingService;

    @Autowired
    public PasswordService(PasswordResetTokenRepository passwordResetTokenRepository,
                           UserRepository userRepository, LoginRepository loginRepository,
                           PortalUserDetailsService userDetailsService, PasswordEncoder passwordEncoder,
                           EmailMessagingService emailMessagingService) {
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
        PasswordResetTokenEntity passwordResetTokenToSave = new PasswordResetTokenEntity(email, passwordResetToken, LocalDateTime.now());
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
        Optional<LocalDateTime> optionalTimeGenerated = passwordResetTokenRepository.findById(passwordResetTokenPK)
                .map(PasswordResetTokenEntity::getDateGenerated);

        if (optionalTimeGenerated.isPresent()) {
            Duration timeSinceTokenGenerated = Duration.between(optionalTimeGenerated.get(), LocalDateTime.now());
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
    public boolean updatePassword(HttpServletResponse response, UpdatePasswordModel updatePasswordModel) {
        Optional<UsernamePasswordAuthenticationToken> optionalUserAuthToken = SecurityContextUtils.retrieveUserAuthToken();
        if (optionalUserAuthToken.isPresent()) {
            UsernamePasswordAuthenticationToken userAuthToken = optionalUserAuthToken.get();
            // TODO this is a redundant check and can be removed
            if (canUpdatePassword(userAuthToken)) {
                UserDetails userDetails = SecurityContextUtils.extractUserDetails(userAuthToken);

                String authenticatedUserEmail = userDetails.getUsername();
                boolean wasSaveSuccessful = persistPasswordUpdate(authenticatedUserEmail, updatePasswordModel.getNewPassword());
                if (wasSaveSuccessful) {
                    response.setHeader("Location", "/");
                    clearResetPasswordAuthorityFromSecurityContext();
                    return true;
                }
            }
        }

        log.error("The password could not be updated");
        return false;
    }

    private boolean canUpdatePassword(UsernamePasswordAuthenticationToken auth) {
        return auth.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(PortalAuthorityConstants.UPDATE_PASSWORD_PERMISSION::equals);
    }

    private boolean persistPasswordUpdate(String email, String password) {
        if (StringUtils.isBlank(email) || StringUtils.isBlank(password)) {
            log.error("Blank credential(s) provided");
            return false;
        }

        Optional<LoginEntity> optionalLoginEntity = userRepository.findFirstByEmail(email)
                .map(UserEntity::getUserId)
                .map(loginRepository::findFirstByUserId)
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
                user, null, Collections.singletonList(new SimpleGrantedAuthority(PortalAuthorityConstants.UPDATE_PASSWORD_PERMISSION)));
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    private void clearResetPasswordAuthorityFromSecurityContext() {
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    private boolean sendPasswordResetEmail(String email, String passwordResetToken) {
        // TODO implement
        String url = String.format("localhost:8080/api/password/reset/validate?token=%s&email=%s", passwordResetToken, email);
        log.info("*** REMOVE ME *** Password Reset Token: " + passwordResetToken);
        log.info("*** REMOVE ME *** Password Reset URL: " + url);

        // TODO create email message
        EmailMessage emailMessage = null;

        try {
            emailMessagingService.sendMessage(emailMessage);
            return true;
        } catch (PortalEmailException e) {
            log.error("Problem sending password reset email", e);
        }
        return false;
    }

}
