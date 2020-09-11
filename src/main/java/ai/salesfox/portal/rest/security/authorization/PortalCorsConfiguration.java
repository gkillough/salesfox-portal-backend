package ai.salesfox.portal.rest.security.authorization;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
public class PortalCorsConfiguration {
    @Value("${ai.salesfox.portal.cors.allowed.origins:*}")
    private String allowedOrigins;

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
