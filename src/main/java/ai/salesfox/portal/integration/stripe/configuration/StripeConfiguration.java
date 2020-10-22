package ai.salesfox.portal.integration.stripe.configuration;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource(StripeConfiguration.STRIPE_CONFIGURATION_FILE_NAME)
public class StripeConfiguration {
    public static final String STRIPE_CONFIGURATION_FILE_NAME = "stripe.properties";

    @Getter
    @Value("${ai.salesfox.portal.integration.stripe.key.secret:}")
    private String stripeSecretKey;

}
