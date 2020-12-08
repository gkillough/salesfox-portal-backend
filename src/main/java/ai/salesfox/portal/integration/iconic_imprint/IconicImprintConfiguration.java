package ai.salesfox.portal.integration.iconic_imprint;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Getter
@Configuration
@PropertySource(IconicImprintConfiguration.ICONIC_IMPRINT_CONFIGURATION_FILE_NAME)
public class IconicImprintConfiguration {
    public static final String ICONIC_IMPRINT_CONFIGURATION_FILE_NAME = "iconic_imprint.properties";

    @Value("{ai.salesfox.portal.integration.iconic.imprint.order.email.address:}")
    private String iconicImprintOrderEmailAddress;

}
