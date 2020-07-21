package com.getboostr.portal;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

public class GeneratePasswordHashTest {

    @Test
    public void generatePasswordHashTest() {
        // Set this string to the password you want to hash:
        String plainTextPassword = "testPass";

        PortalConfiguration portalConfiguration = new PortalConfiguration();
        PasswordEncoder passwordEncoder = portalConfiguration.defaultPasswordEncoder();

        String passwordHash = passwordEncoder.encode(plainTextPassword);
        System.out.println("Test Password Hash: " + passwordHash);
    }

}
