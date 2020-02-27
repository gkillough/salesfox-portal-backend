package com.usepipeline.portal.web.password;

import com.usepipeline.portal.common.service.email.EmailMessage;
import com.usepipeline.portal.common.service.email.EmailMessagingService;
import com.usepipeline.portal.database.authentication.entity.PasswordResetTokenEntity;
import com.usepipeline.portal.database.authentication.entity.UserEntity;
import com.usepipeline.portal.database.authentication.key.PasswordResetTokenPK;
import com.usepipeline.portal.database.authentication.repository.PasswordResetTokenRepository;
import com.usepipeline.portal.database.authentication.repository.UserRepository;
import com.usepipeline.portal.web.security.authentication.PortalUserDetailsService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletResponse;
import java.sql.Date;
import java.time.Duration;
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
    private PortalUserDetailsService userDetailsService;
    private EmailMessagingService emailMessagingService;

    @Autowired
    public PasswordService(PasswordResetTokenRepository passwordResetTokenRepository, UserRepository userRepository, PortalUserDetailsService userDetailsService, EmailMessagingService emailMessagingService) {
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.userRepository = userRepository;
        this.userDetailsService = userDetailsService;
        this.emailMessagingService = emailMessagingService;
    }

    public boolean sendPasswordResetEmail(ResetPasswordModel resetPasswordModel) {
        String email = resetPasswordModel.getEmail();
        if (StringUtils.isBlank(email)) {
            log.error("The field 'email' cannot be blank");
            return false;
        }

        Optional<UserEntity> optionalUser = userRepository.findByEmail(email);
        if (!optionalUser.isPresent()) {
            log.error("No user with that email exists");
            return false;
        }

        String passwordResetToken = UUID.randomUUID().toString();
        Date dateGenerated = new Date(System.currentTimeMillis());
        PasswordResetTokenEntity passwordResetTokenToSave = new PasswordResetTokenEntity(email, passwordResetToken, dateGenerated);
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
        Optional<Long> optionalTimeGenerated = passwordResetTokenRepository.findById(passwordResetTokenPK)
                .map(PasswordResetTokenEntity::getDateGenerated)
                .map(Date::getTime);

        if (optionalTimeGenerated.isPresent()) {
            long currentTime = System.currentTimeMillis();
            long timeSinceTokenGenerated = currentTime - optionalTimeGenerated.get();

            Duration duration = Duration.ofMillis(timeSinceTokenGenerated);
            if (duration.compareTo(DURATION_OF_TOKEN_VALIDITY) < 0) {
                passwordResetTokenRepository.deleteById(passwordResetTokenPK);
                grantTemporaryAuthorityToUser(email);
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

    private void grantTemporaryAuthorityToUser(String email) {
        UserDetails user = userDetailsService.loadUserByUsername(email);

        Authentication auth = new UsernamePasswordAuthenticationToken(
                user, null, Collections.singletonList(new SimpleGrantedAuthority(UPDATE_PASSWORD_AUTHORITY_NAME)));
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    public boolean updatePassword(UpdatePasswordModel updatePasswordModel) {
        // TODO implement
        return false;
    }

    private EmailMessage createEmailMessage(String email, String passwordResetToken) {
        // TODO implement
        String url = String.format("localhost:8080/password/reset/validate?token=%s&email=%s", passwordResetToken, email);
        log.info("*** REMOVE ME *** Password Reset Token: " + passwordResetToken);
        log.info("*** REMOVE ME *** Password Reset URL: " + url);
        return null;
    }

}
