package com.usepipeline.portal.web.user.common;

import com.usepipeline.portal.database.authentication.entity.UserEntity;
import com.usepipeline.portal.database.authentication.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        // TODO abstract this security context handling functionality into its own service
        //  this controller as well as PasswordService use many of these steps
        if (UsernamePasswordAuthenticationToken.class.isInstance(auth)) {
            UsernamePasswordAuthenticationToken usernamePasswordAuth = (UsernamePasswordAuthenticationToken) auth;
            // If we have a UsernamePasswordAuthenticationToken, then the principal must be a UserDetails object.
            UserDetails userDetails = (UserDetails) usernamePasswordAuth.getPrincipal();

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
