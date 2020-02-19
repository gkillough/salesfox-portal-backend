package com.usepipeline.portal.web.login;

import org.springframework.stereotype.Component;

@Component
public class LoginActions {

    public boolean handleLogin(String username, String password) {
        // TODO implement

        // Find user in the database

        // Create password hash with salt from provided password

        // Compare to database password hash
        return false;
    }

    public boolean handleResetPassword(String username) {
        // TODO implement

        // Find user in the database

        // Get email


        return false;
    }

}
