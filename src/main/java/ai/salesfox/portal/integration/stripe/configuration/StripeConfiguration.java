package ai.salesfox.portal.integration.stripe.configuration;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Data
@Configuration
@PropertySource(StripeConfiguration.STRIPE_CONFIGURATION_FILE_NAME)
public class StripeConfiguration {
    public static final String STRIPE_CONFIGURATION_FILE_NAME = "stripe.properties";

    @Value("${stripe.key.secret}")
    private String stripeSecretKey;

}
