package com.getboostr.portal.rest.security.authentication.user;

import com.getboostr.portal.common.time.PortalDateTimeUtils;
import com.getboostr.portal.database.account.entity.LoginEntity;
import com.getboostr.portal.database.account.entity.UserEntity;
import com.getboostr.portal.database.account.repository.LoginRepository;
import com.getboostr.portal.database.account.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

@Component
@Slf4j
public class PortalUserLoginAttemptService {
    public static final Duration DURATION_UNTIL_ACCOUNT_UNLOCKED = Duration.ofMinutes(30L);
    public static final int MAX_LOGIN_ATTEMPTS = 5;

    public UserRepository userRepository;
    public LoginRepository loginRepository;

    @Autowired
    public PortalUserLoginAttemptService(UserRepository userRepository, LoginRepository loginRepository) {
        this.userRepository = userRepository;
        this.loginRepository = loginRepository;
    }

    public void recordFailedAttempt(String username) {
        Optional<LoginEntity> optionalLogin = getLoginFromDetails(username);
        if (optionalLogin.isPresent()) {
            LoginEntity userLogin = optionalLogin.get();
            int numFailedLogins = userLogin.getNumFailedLogins() + 1;
            userLogin.setNumFailedLogins(numFailedLogins);
            if (numFailedLogins % MAX_LOGIN_ATTEMPTS == 0) {
                // Set last locked time only when they reach exactly the max number of attempts
                log.warn("Maximum login attempts reached for user [{}]. Locking account.", username);
                userLogin.setLastLocked(PortalDateTimeUtils.getCurrentDateTimeUTC());
            }

            loginRepository.save(userLogin);
        }
    }

    public void recordSuccessfulAttempt(String username) {
        Optional<LoginEntity> optionalLogin = getLoginFromDetails(username);
        if (optionalLogin.isPresent()) {
            LoginEntity userLogin = optionalLogin.get();
            userLogin.setNumFailedLogins(0);
            userLogin.setLastLocked(null);
            userLogin.setLastSuccessfulLogin(PortalDateTimeUtils.getCurrentDateTimeUTC());

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
                .flatMap(loginRepository::findById);
    }

}
