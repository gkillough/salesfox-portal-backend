package ai.salesfox.portal.integration.scribeless.configuration;

import ai.salesfox.integration.common.http.HttpServiceWrapper;
import ai.salesfox.integration.common.http.HttpServicesFactory;
import ai.salesfox.integration.scribeless.model.ApiKeyHolder;
import ai.salesfox.integration.scribeless.service.on_demand.OnDemandPreviewService;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.net.Proxy;

@Data
@Configuration
@PropertySource(ScribelessConfiguration.SCRIBELESS_CONFIGURATION_FILE_NAME)
public class ScribelessConfiguration {
    public static final String SCRIBELESS_CONFIGURATION_FILE_NAME = "scribeless.properties";

    @Value("${ai.salesfox.portal.integration.scribeless.base.url:}")
    private String scribelessBaseUrl;

    @Value("${ai.salesfox.portal.integration.scribeless.api.key:}")
    private CharSequence scribelessApiKey;

    @Bean
    public OnDemandPreviewService scribelessOnDemandPreviewService() {
        return new OnDemandPreviewService(scribelessApiKeyHolder(), scribelessHttpServiceWrapper());
    }

    @Bean
    public HttpServiceWrapper scribelessHttpServiceWrapper() {
        // TODO use portal global proxy config
        return HttpServicesFactory.withProxy(scribelessBaseUrl, Proxy.NO_PROXY);
    }

    @Bean
    public ApiKeyHolder scribelessApiKeyHolder() {
        return new ApiKeyHolder(scribelessApiKey);
    }

}
