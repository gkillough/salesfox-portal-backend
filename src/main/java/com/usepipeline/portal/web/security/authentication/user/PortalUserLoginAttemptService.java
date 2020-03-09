package com.usepipeline.portal.web.security.authentication.user;

import com.usepipeline.portal.database.authentication.entity.LoginEntity;
import com.usepipeline.portal.database.authentication.entity.UserEntity;
import com.usepipeline.portal.database.authentication.repository.LoginRepository;
import com.usepipeline.portal.database.authentication.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.sql.Date;
import java.util.Optional;

@Component
public class PortalUserLoginAttemptService {
    public static final int MAX_LOGIN_ATTEMPTS = 10;

    public UserRepository userRepository;
    public LoginRepository loginRepository;

    @Autowired
    public PortalUserLoginAttemptService(UserRepository userRepository, LoginRepository loginRepository) {
        this.userRepository = userRepository;
        this.loginRepository = loginRepository;
    }

    /**
     * @param userDetails the UserDetails instance of the user who attempted to log in
     * @return true if the maximum number of login attempts has been exceeded
     */
    public boolean addAttempt(UserDetails userDetails) {
        Optional<LoginEntity> optionalLogin = getLoginFromDetails(userDetails);
        if (optionalLogin.isPresent()) {
            LoginEntity userLogin = optionalLogin.get();
            int numFailedLogins = userLogin.getNumFailedLogins() + 1;
            userLogin.setNumFailedLogins(numFailedLogins);

            loginRepository.save(userLogin);
            return numFailedLogins >= MAX_LOGIN_ATTEMPTS;
        }
        return false;
    }

    /**
     * @param userDetails the UserDetails instance of the user who attempted to log in
     * @return the number of attempts on the account after decrementing
     */
    public int decrementAttempts(UserDetails userDetails) {
        Optional<LoginEntity> optionalLogin = getLoginFromDetails(userDetails);
        if (optionalLogin.isPresent()) {
            LoginEntity userLogin = optionalLogin.get();
            if (userLogin.getNumFailedLogins() > 0) {
                int numFailedLogins = userLogin.getNumFailedLogins() - 1;
                userLogin.setNumFailedLogins(numFailedLogins);

                loginRepository.save(userLogin);
                return numFailedLogins;
            }
        }
        return 0;
    }

    public void resetAttempts(UserDetails userDetails) {
        Optional<LoginEntity> optionalLogin = getLoginFromDetails(userDetails);
        if (optionalLogin.isPresent()) {
            LoginEntity userLogin = optionalLogin.get();
            userLogin.setNumFailedLogins(0);
            userLogin.setLastSuccessfulLogin(new Date(System.currentTimeMillis()));

            loginRepository.save(userLogin);
        }
    }

    public Optional<LoginEntity> getLoginFromDetails(UserDetails userDetails) {
        if (null == userDetails.getUsername()) {
            return Optional.empty();
        }

        return userRepository
                .findFirstByEmail(userDetails.getUsername())
                .map(UserEntity::getUserId)
                .flatMap(loginRepository::findFirstByUserId);
    }

}
