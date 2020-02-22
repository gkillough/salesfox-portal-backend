package com.usepipeline.portal.web.login;

import com.usepipeline.portal.database.authentication.entity.LoginEntity;
import com.usepipeline.portal.database.authentication.repository.LoginRepository;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class UserLoginService {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private LoginRepository loginRepository;
    private PasswordEncoder passwordEncoder;

    @Autowired
    public UserLoginService(LoginRepository loginRepository, PasswordEncoder passwordEncoder) {
        this.loginRepository = loginRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public boolean handleLogin(String username, String password) {
        if (StringUtils.isBlank(username)) {
            logger.warn("Login was attempted with a blank username");
            return false;
        }

        if (StringUtils.isBlank(password)) {
            logger.warn("Login was attempted with a blank password");
        }

        Optional<LoginEntity> foundLogin = loginRepository.findByEmail(username);
        if (foundLogin.isPresent()) {
            logger.debug("Found valid login for username {}", username);
            LoginEntity userLogin = foundLogin.get();

            String encodedPassword = encodePassword(password, userLogin.getPasswordSalt());
            if (encodedPassword.equals(userLogin.getPasswordHash())) {
                logger.debug("Credentials valid for user: {}", username);
                return true;
            }
        }
        return false;
    }

    public boolean handleResetPassword(String username) {
        // TODO implement

        // Find user in the database

        // Get email


        return false;
    }

    private String encodePassword(String rawPassword, String salt) {
        return passwordEncoder.encode(rawPassword + salt);
    }

}
