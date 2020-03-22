package com.usepipeline.portal.web.security.authentication.user;

import com.usepipeline.portal.database.authentication.entity.LoginEntity;
import com.usepipeline.portal.database.authentication.entity.UserEntity;
import com.usepipeline.portal.database.authentication.repository.LoginRepository;
import com.usepipeline.portal.database.authentication.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

@Component
@Slf4j
public class PortalUserLoginAttemptService {
    public static final Duration DURATION_UNTIL_ACCOUNT_UNLOCKED = Duration.ofMinutes(2L);
    public static final int MAX_LOGIN_ATTEMPTS = 5;

    public UserRepository userRepository;
    public LoginRepository loginRepository;

    @Autowired
    public PortalUserLoginAttemptService(UserRepository userRepository, LoginRepository loginRepository) {
        this.userRepository = userRepository;
        this.loginRepository = loginRepository;
    }

    public void addAttempt(String username) {
        Optional<LoginEntity> optionalLogin = getLoginFromDetails(username);
        if (optionalLogin.isPresent()) {
            LoginEntity userLogin = optionalLogin.get();
            int numFailedLogins = userLogin.getNumFailedLogins() + 1;
            userLogin.setNumFailedLogins(numFailedLogins);
            if (numFailedLogins == MAX_LOGIN_ATTEMPTS) {
                // Set last locked time only when they reach exactly the max number of attempts
                log.warn("Maximum login attempts reached for user [{}]. Locking account.", username);
                userLogin.setLastLocked(LocalDateTime.now());
            }

            loginRepository.save(userLogin);
        }
    }

    public void resetAttempts(String username, boolean isLoginSuccess) {
        Optional<LoginEntity> optionalLogin = getLoginFromDetails(username);
        if (optionalLogin.isPresent()) {
            LoginEntity userLogin = optionalLogin.get();
            userLogin.setNumFailedLogins(0);
            userLogin.setLastLocked(null);
            if (isLoginSuccess) {
                userLogin.setLastSuccessfulLogin(LocalDateTime.now());
            }

            loginRepository.save(userLogin);
            log.info("Login attempts have been reset for user [{}]", username);
        }
    }

    public Optional<LoginEntity> getLoginFromDetails(String username) {
        if (StringUtils.isBlank(username)) {
            return Optional.empty();
        }

        return userRepository
                .findFirstByEmail(username)
                .map(UserEntity::getUserId)
                .flatMap(loginRepository::findFirstByUserId);
    }

}
