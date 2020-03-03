package com.usepipeline.portal.web.password;

import com.usepipeline.portal.common.service.email.EmailMessage;
import com.usepipeline.portal.common.service.email.EmailMessagingService;
import com.usepipeline.portal.database.authentication.entity.LoginEntity;
import com.usepipeline.portal.database.authentication.entity.PasswordResetTokenEntity;
import com.usepipeline.portal.database.authentication.entity.UserEntity;
import com.usepipeline.portal.database.authentication.key.PasswordResetTokenPK;
import com.usepipeline.portal.database.authentication.repository.LoginRepository;
import com.usepipeline.portal.database.authentication.repository.PasswordResetTokenRepository;
import com.usepipeline.portal.database.authentication.repository.UserRepository;
import com.usepipeline.portal.web.security.authentication.PortalUserDetailsService;
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
    public static final String UPDATE_PASSWORD_AUTHORITY_NAME = "UPDATE_PASSWORD_PERMISSION";

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

        EmailMessage emailMessage = createEmailMessage(email, passwordResetToken);
        return emailMessagingService.sendMessage(emailMessage);
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
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (UsernamePasswordAuthenticationToken.class.isInstance(auth)) {
            UsernamePasswordAuthenticationToken usernamePasswordAuth = (UsernamePasswordAuthenticationToken) auth;
            if (canUpdatePassword(usernamePasswordAuth)) {
                // If we have a UsernamePasswordAuthenticationToken, then the principal must be a UserDetails object.
                UserDetails userDetails = (UserDetails) usernamePasswordAuth.getPrincipal();
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
                .anyMatch(UPDATE_PASSWORD_AUTHORITY_NAME::equals);
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
                user, null, Collections.singletonList(new SimpleGrantedAuthority(UPDATE_PASSWORD_AUTHORITY_NAME)));
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    private void clearResetPasswordAuthorityFromSecurityContext() {
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    private EmailMessage createEmailMessage(String email, String passwordResetToken) {
        // TODO implement
        String url = String.format("localhost:8080/password/reset/validate?token=%s&email=%s", passwordResetToken, email);
        log.info("*** REMOVE ME *** Password Reset Token: " + passwordResetToken);
        log.info("*** REMOVE ME *** Password Reset URL: " + url);
        return null;
    }

}
