package ai.salesfox.portal.common.service.email;

import freemarker.cache.ClassTemplateLoader;
import freemarker.cache.FileTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.TemplateExceptionHandler;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

import java.io.File;

@Slf4j
@org.springframework.context.annotation.Configuration
public class FreemarkerConfiguration {
    @Getter
    @Value("file:${com.getboostr.portal.resource.freemarker.templateDir:}")
    private File freemarkerTemplateDir;

    @Bean
    public Configuration createFreemarkerConfig() {
        Configuration configuration = createDefaultConfiguration();
        configuration.setTemplateLoader(createTemplateLoader());
        return configuration;
    }

    private Configuration createDefaultConfiguration() {
        Configuration defaultConfiguration = new Configuration(Configuration.VERSION_2_3_25);
        defaultConfiguration.setDefaultEncoding("UTF-8");
        defaultConfiguration.setTemplateExceptionHandler(TemplateExceptionHandler.HTML_DEBUG_HANDLER);
        defaultConfiguration.setLogTemplateExceptions(false);
        return defaultConfiguration;
    }

    private TemplateLoader createTemplateLoader() {
        try {
            return new FileTemplateLoader(freemarkerTemplateDir, false);
        } catch (Exception e) {
            log.warn("Could not initialize Freemarker FileTemplateLoader. Falling back to ClassTemplateLoader", e);
            return new ClassTemplateLoader(getClass(), "/templates/");
        }
    }

}
