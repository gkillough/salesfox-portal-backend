package ai.salesfox.portal.rest.security;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.apache.tomcat.util.http.Rfc6265CookieProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PortalSessionCookieConfiguration {
    @Getter
    @Value("${ai.salesfox.portal.session.cookie.domain:}")
    private String portalCookieDomain;

    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> portalWebServerCustomizer() {
        return factory -> {
            factory.addContextCustomizers(customizer -> {
                Rfc6265CookieProcessor cookieProcessor = new Rfc6265CookieProcessor();
                // In production, the front-end and back-end are served from two domains,
                // so we need to allow the cookies to be shared.
                cookieProcessor.setSameSiteCookies("None");
                customizer.setCookieProcessor(cookieProcessor);
                if (StringUtils.isNotBlank(portalCookieDomain)) {
                    customizer.setSessionCookieDomain(portalCookieDomain);
                }
            });
        };
    }

}
