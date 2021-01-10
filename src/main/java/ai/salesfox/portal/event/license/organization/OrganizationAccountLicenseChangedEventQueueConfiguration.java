package ai.salesfox.portal.event.license.organization;

import ai.salesfox.portal.event.EventQueueConstants;
import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OrganizationAccountLicenseChangedEventQueueConfiguration {
    private static final String LICENSE_CHANGED = "LICENSE_CHANGED";
    public static final String LICENSE_CHANGED_QUEUE = LICENSE_CHANGED + EventQueueConstants.QUEUE_SUFFIX;
    public static final String LICENSE_CHANGED_EXCHANGE = LICENSE_CHANGED + EventQueueConstants.EXCHANGE_SUFFIX;

    public static final String LICENSE_CHANGED_DLQ = LICENSE_CHANGED + EventQueueConstants.DLQ_SUFFIX;
    public static final String LICENSE_CHANGED_DLQ_EXCHANGE = LICENSE_CHANGED + EventQueueConstants.DLQ_SUFFIX + EventQueueConstants.EXCHANGE_SUFFIX;

    @Bean
    public Queue licenseChangedQueue() {
        return QueueBuilder
                .durable(LICENSE_CHANGED_QUEUE)
                .withArgument(EventQueueConstants.KEY_DEAD_LETTER_EXCHANGE, LICENSE_CHANGED_DLQ_EXCHANGE)
                .withArgument(EventQueueConstants.KEY_DEAD_LETTER_EXCHANGE_ROUTING_KEY, LICENSE_CHANGED_DLQ)
                .build();
    }

    @Bean
    public Exchange licenseChangedExchange() {
        return ExchangeBuilder
                .directExchange(LICENSE_CHANGED_EXCHANGE)
                .durable(true)
                .build();
    }

    @Bean
    public Binding licenseChangedBinding() {
        return BindingBuilder
                .bind(licenseChangedQueue())
                .to(licenseChangedExchange())
                .with(LICENSE_CHANGED_QUEUE)
                .noargs();
    }

    // Dead Letter Queue

    @Bean
    public Queue licenseChangedDLQ() {
        return QueueBuilder
                .durable(LICENSE_CHANGED_DLQ)
                .build();
    }

    @Bean
    public Exchange licenseChangedDLQExchange() {
        return ExchangeBuilder
                .directExchange(LICENSE_CHANGED_DLQ_EXCHANGE)
                .durable(true)
                .build();
    }

    @Bean
    public Binding licenseChangedDLQBinding() {
        return BindingBuilder
                .bind(licenseChangedDLQ())
                .to(licenseChangedDLQExchange())
                .with(LICENSE_CHANGED_DLQ)
                .noargs();
    }

}
