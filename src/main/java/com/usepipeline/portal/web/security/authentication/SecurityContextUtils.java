package com.usepipeline.portal.web.security.authentication;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

public class SecurityContextUtils {
    public static Optional<UsernamePasswordAuthenticationToken> retrieveUserAuthToken() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (UsernamePasswordAuthenticationToken.class.isInstance(auth)) {
            UsernamePasswordAuthenticationToken usernamePasswordAuth = (UsernamePasswordAuthenticationToken) auth;
            return Optional.of(usernamePasswordAuth);
        }
        return Optional.empty();
    }

    public static UserDetails extractUserDetails(UsernamePasswordAuthenticationToken userAuthToken) {
        // If we have a UsernamePasswordAuthenticationToken, then the principal must be a UserDetails object.
        return (UserDetails) userAuthToken.getPrincipal();
    }

}
