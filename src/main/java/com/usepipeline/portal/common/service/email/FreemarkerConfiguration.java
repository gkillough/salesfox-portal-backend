package com.usepipeline.portal.common.service.email;

import freemarker.cache.ClassTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.TemplateExceptionHandler;
import org.springframework.context.annotation.Bean;

@org.springframework.context.annotation.Configuration
public class FreemarkerConfiguration {
    // TODO read this as a spring property
    public static final String DEFAULT_TEMPLATE_DIR_PATH = "/templates";

    @Bean
    public Configuration createFreemarkerConfig() {
        Configuration configuration = createDefaultConfiguration();
        configuration.setTemplateLoader(createClassTemplateLoader());
        return configuration;
    }

    private Configuration createDefaultConfiguration() {
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_25);

        cfg.setDefaultEncoding("UTF-8");
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.HTML_DEBUG_HANDLER);
        cfg.setLogTemplateExceptions(false);

        return cfg;
    }

    private TemplateLoader createClassTemplateLoader() {
        return new ClassTemplateLoader(getClass(), DEFAULT_TEMPLATE_DIR_PATH);
    }

}
