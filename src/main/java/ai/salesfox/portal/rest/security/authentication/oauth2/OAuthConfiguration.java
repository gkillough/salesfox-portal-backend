package ai.salesfox.portal.rest.security.authentication.oauth2;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.*;

public class OAuthConfiguration {
    @Value("${auth0.audience:}")
    private String audience;

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri:}")
    private String issuer;

    @Bean
    public JwtDecoder portalJwtDecoder() {
        NimbusJwtDecoder jwtDecoder = (NimbusJwtDecoder)
                JwtDecoders.fromOidcIssuerLocation(issuer);

        OAuth2TokenValidator<Jwt> audienceValidator = new OAuthAudienceValidator(audience);
        OAuth2TokenValidator<Jwt> issuerValidator = JwtValidators.createDefaultWithIssuer(issuer);
        OAuth2TokenValidator<Jwt> combinedValidator = new DelegatingOAuth2TokenValidator<>(issuerValidator, audienceValidator);

        jwtDecoder.setJwtValidator(combinedValidator);

        return jwtDecoder;
    }

}
