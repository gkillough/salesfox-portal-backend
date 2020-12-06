package ai.salesfox.portal.common.service.email;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Getter
@Configuration
@PropertySource(PortalEmailAddressConfiguration.EMAIL_ADDRESS_CONFIGURATION_FILE_NAME)
public class PortalEmailAddressConfiguration {
    public static final String EMAIL_ADDRESS_CONFIGURATION_FILE_NAME = "portal_email_address.properties";

    @Value("${ai.salesfox.portal.email.address.noreply:}")
    private String noreplyEmailAddress;

    @Value("${ai.salesfox.portal.email.address.support:}")
    private String supportEmailAddress;

    @Value("${ai.salesfox.portal.email.address.orders:}")
    private String ordersEmailAddress;

}
