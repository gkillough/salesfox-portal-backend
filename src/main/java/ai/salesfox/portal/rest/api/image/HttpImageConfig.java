package ai.salesfox.portal.rest.api.image;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.BufferedImageHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class HttpImageConfig implements WebMvcConfigurer {
    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.add(bufferedImageHttpMessageConverter());
    }

    @Bean
    public BufferedImageHttpMessageConverter bufferedImageHttpMessageConverter() {
        return new BufferedImageHttpMessageConverter();
    }

}
