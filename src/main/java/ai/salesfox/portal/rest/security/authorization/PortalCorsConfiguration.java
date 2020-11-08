package ai.salesfox.portal.rest.security.authorization;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
public class PortalCorsConfiguration {
    @Value("${ai.salesfox.portal.cors.allowed.origins:}")
    private String allowedOrigins;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration corsConfiguration = new CorsConfiguration().applyPermitDefaultValues();
        corsConfiguration.setAllowedOrigins(getAllowedOrigins());
        corsConfiguration.setAllowCredentials(true);
        corsConfiguration.setAllowedHeaders(
                List.of(
                        "origin",
                        "content-type",
                        "accept",
                        "Cookie",
                        "X-CSRF-TOKEN"
                )
        );
        corsConfiguration.setAllowedMethods(
                List.of(
                        HttpMethod.OPTIONS.name(),
                        HttpMethod.GET.name(),
                        HttpMethod.POST.name(),
                        HttpMethod.PUT.name(),
                        HttpMethod.PATCH.name(),
                        HttpMethod.DELETE.name()
                )
        );

        UrlBasedCorsConfigurationSource corsConfigSource = new UrlBasedCorsConfigurationSource();
        corsConfigSource.registerCorsConfiguration("/**", corsConfiguration);
        return corsConfigSource;
    }

    public List<String> getAllowedOrigins() {
        if (StringUtils.isBlank(allowedOrigins)) {
            return List.of();
        }

        String[] splitAllowedOrigins = StringUtils.split(allowedOrigins, ",");
        return Arrays
                .stream(splitAllowedOrigins)
                .map(StringUtils::trim)
                .collect(Collectors.toUnmodifiableList());
    }

}
