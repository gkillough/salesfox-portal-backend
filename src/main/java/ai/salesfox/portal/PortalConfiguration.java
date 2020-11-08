package ai.salesfox.portal;

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
    @Value("${ai.salesfox.portal.backEndUrl:}")
    private String portalBackEndUrl;

    @Getter
    @Value("${ai.salesfox.portal.frontEndUrl:}")
    private String portalFrontEndUrl;

    @Getter
    @Value("${ai.salesfox.portal.frontEndResetPasswordRoute:}")
    private String frontEndResetPasswordRoute;

    @Getter
    @Value("${ai.salesfox.portal.frontEndOrgAcctInviteRoute:}")
    private String frontEndOrgAcctInviteRoute;

    @Bean
    public HttpSessionCsrfTokenRepository csrfTokenRepository() {
        return new HttpSessionCsrfTokenRepository();
    }

    @Bean
    public PasswordEncoder defaultPasswordEncoder() {
        return new BCryptPasswordEncoder(13);
    }

}
