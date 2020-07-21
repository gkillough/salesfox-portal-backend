package com.getboostr.portal.common.file_system;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
public class ResourceDirectoryConfiguration {
    @Value("${com.usepipeline.portal.resource.baseDir:}")
    private String resourceBaseDir;

    @Value("${com.usepipeline.portal.resource.iconDir:}")
    private String iconDir;

}
