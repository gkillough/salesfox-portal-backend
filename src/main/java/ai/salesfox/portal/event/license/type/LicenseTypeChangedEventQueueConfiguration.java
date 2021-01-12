package ai.salesfox.portal.event.license.type;

import ai.salesfox.portal.event.EventQueueConstants;
import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LicenseTypeChangedEventQueueConfiguration {
    private static final String LICENSE_TYPE_CHANGED = "LICENSE_TYPE_CHANGED";
    public static final String LICENSE_TYPE_CHANGED_QUEUE = LICENSE_TYPE_CHANGED + EventQueueConstants.QUEUE_SUFFIX;
    public static final String LICENSE_TYPE_CHANGED_EXCHANGE = LICENSE_TYPE_CHANGED + EventQueueConstants.EXCHANGE_SUFFIX;

    public static final String LICENSE_TYPE_CHANGED_DLQ = LICENSE_TYPE_CHANGED + EventQueueConstants.DLQ_SUFFIX;
    public static final String LICENSE_TYPE_CHANGED_DLQ_EXCHANGE = LICENSE_TYPE_CHANGED + EventQueueConstants.DLQ_SUFFIX + EventQueueConstants.EXCHANGE_SUFFIX;

    @Bean
    public Queue licenseTypeChangedQueue() {
        return QueueBuilder
                .durable(LICENSE_TYPE_CHANGED_QUEUE)
                .withArgument(EventQueueConstants.KEY_DEAD_LETTER_EXCHANGE, LICENSE_TYPE_CHANGED_DLQ_EXCHANGE)
                .withArgument(EventQueueConstants.KEY_DEAD_LETTER_EXCHANGE_ROUTING_KEY, LICENSE_TYPE_CHANGED_DLQ)
                .withArgument(EventQueueConstants.KEY_SINGLE_ACTIVE_CONSUMER, true)
                .build();
    }

    @Bean
    public Exchange licenseTypeChangedExchange() {
        return ExchangeBuilder
                .directExchange(LICENSE_TYPE_CHANGED_EXCHANGE)
                .durable(true)
                .build();
    }

    @Bean
    public Binding licenseTypeChangedBinding() {
        return BindingBuilder
                .bind(licenseTypeChangedQueue())
                .to(licenseTypeChangedExchange())
                .with(LICENSE_TYPE_CHANGED_QUEUE)
                .noargs();
    }

    // Dead Letter Queue

    @Bean
    public Queue licenseTypeChangedDLQ() {
        return QueueBuilder
                .durable(LICENSE_TYPE_CHANGED_DLQ)
                .build();
    }

    @Bean
    public Exchange licenseTypeChangedDLQExchange() {
        return ExchangeBuilder
                .directExchange(LICENSE_TYPE_CHANGED_DLQ_EXCHANGE)
                .durable(true)
                .build();
    }

    @Bean
    public Binding licenseTypeChangedDLQBinding() {
        return BindingBuilder
                .bind(licenseTypeChangedDLQ())
                .to(licenseTypeChangedDLQExchange())
                .with(LICENSE_TYPE_CHANGED_DLQ)
                .noargs();
    }

}
