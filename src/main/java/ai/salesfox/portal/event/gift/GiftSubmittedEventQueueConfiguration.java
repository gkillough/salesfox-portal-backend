package ai.salesfox.portal.event.gift;

import ai.salesfox.portal.event.EventQueueConstants;
import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GiftSubmittedEventQueueConfiguration {
    private static final String GIFT_SUBMITTED = "GIFT_SUBMITTED";
    public static final String GIFT_SUBMITTED_QUEUE = GIFT_SUBMITTED + EventQueueConstants.QUEUE_SUFFIX;
    public static final String GIFT_SUBMITTED_EXCHANGE = GIFT_SUBMITTED + EventQueueConstants.EXCHANGE_SUFFIX;

    public static final String GIFT_SUBMITTED_DLQ = GIFT_SUBMITTED + EventQueueConstants.DLQ_SUFFIX;
    public static final String GIFT_SUBMITTED_DLQ_EXCHANGE = GIFT_SUBMITTED + EventQueueConstants.DLQ_SUFFIX + EventQueueConstants.EXCHANGE_SUFFIX;

    @Bean
    public Queue giftSubmittedQueue() {
        return QueueBuilder
                .durable(GIFT_SUBMITTED_QUEUE)
                .withArgument(EventQueueConstants.KEY_DEAD_LETTER_EXCHANGE, GIFT_SUBMITTED_DLQ_EXCHANGE)
                .build();
    }

    @Bean
    public Exchange giftSubmittedExchange() {
        return ExchangeBuilder
                .directExchange(GIFT_SUBMITTED_EXCHANGE)
                .durable(true)
                .build();
    }

    @Bean
    public Binding giftSubmittedBinding() {
        return BindingBuilder
                .bind(giftSubmittedQueue())
                .to(giftSubmittedExchange())
                .with(GIFT_SUBMITTED_QUEUE)
                .noargs();
    }

    // Dead Letter Queue

    @Bean
    public Queue giftSubmittedDLQ() {
        return new Queue(GIFT_SUBMITTED_DLQ, true);
    }

    @Bean
    public Exchange giftSubmittedDLQExchange() {
        return ExchangeBuilder
                .directExchange(GIFT_SUBMITTED_DLQ_EXCHANGE)
                .durable(true)
                .build();
    }

    @Bean
    public Binding giftSubmittedDLQBinding() {
        return BindingBuilder
                .bind(giftSubmittedDLQ())
                .to(giftSubmittedDLQExchange())
                .with(GIFT_SUBMITTED_QUEUE)
                .noargs();
    }

}
