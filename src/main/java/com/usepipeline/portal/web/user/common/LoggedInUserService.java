package com.usepipeline.portal.web.user.common;

import com.usepipeline.portal.database.authentication.entity.UserEntity;
import com.usepipeline.portal.database.authentication.repository.UserRepository;
import com.usepipeline.portal.web.security.authentication.SecurityContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
public class LoggedInUserService {
    private UserRepository userRepository;

    @Autowired
    public LoggedInUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public LoggedInUserModel getLoggedInUser() {
        Optional<UsernamePasswordAuthenticationToken> optionalUserAuthToken = SecurityContextUtil.retrieveUserAuthToken();
        if (optionalUserAuthToken.isPresent()) {
            UserDetails userDetails = SecurityContextUtil.extractUserDetails(optionalUserAuthToken.get());

            Optional<UserEntity> optionalUser = userRepository.findFirstByEmail(userDetails.getUsername());
            if (optionalUser.isPresent()) {
                UserEntity user = optionalUser.get();
                return new LoggedInUserModel(user.getFirstName(), user.getLastName(), user.getEmail());
            } else {
                log.error("The logged in user is not in the database. Username: [{}]", userDetails.getUsername());
            }
        }

        return LoggedInUserModel.ANONYMOUS_USER;
    }

}
