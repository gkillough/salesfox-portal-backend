package ai.salesfox.portal.integration.noms.configuration;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.io.File;

@Configuration
@PropertySource(NomsConfiguration.NOMS_CONFIGURATION_FILE_NAME)
public class NomsConfiguration {
    public static final String NOMS_CONFIGURATION_FILE_NAME = "noms.properties";

    @Getter
    @Value("{ai.salesfox.portal.integration.noms.temp.directory:}")
    private File nomsTempDirectory;

}
