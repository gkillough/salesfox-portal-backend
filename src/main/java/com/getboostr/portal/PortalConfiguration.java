package com.getboostr.portal;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;

@Configuration
public class PortalConfiguration {
    @Getter
    @Value("${com.getboostr.portal.baseUrl:}")
    private String portalBaseUrl;
    @Getter
    @Value("${com.getboostr.portal.resetPasswordLinkSpec:}")
    private String resetPasswordLinkSpec;
    @Getter
    @Value("${com.getboostr.portal.inviteOrganizationAccountUserLinkSpec:}")
    private String inviteOrganizationAccountUserLinkSpec;

    @Bean
    public HttpSessionCsrfTokenRepository csrfTokenRepository() {
        return new HttpSessionCsrfTokenRepository();
    }

    @Bean
    public PasswordEncoder defaultPasswordEncoder() {
        return new BCryptPasswordEncoder(8);
    }

}
