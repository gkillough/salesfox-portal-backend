package ai.salesfox.portal.common.file_system;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
public class ResourceDirectoryConfiguration {
    @Value("${ai.salesfox.portal.resource.baseDir:}")
    private String resourceBaseDir;

    @Value("${ai.salesfox.portal.resource.iconDir:}")
    private String iconDir;

}
