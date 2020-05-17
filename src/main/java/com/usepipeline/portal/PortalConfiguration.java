package com.usepipeline.portal;

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
    @Value("${com.usepipeline.portal.baseUrl:}")
    private String portalBaseUrl;
    @Getter
    @Value("${com.usepipeline.portal.resetPasswordLinkSpec:}")
    private String resetPasswordLinkSpec;
    @Getter
    @Value("${com.usepipeline.portal.inviteOrganizationAccountUserLinkSpec:}")
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
