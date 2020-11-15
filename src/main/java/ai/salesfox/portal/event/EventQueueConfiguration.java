package ai.salesfox.portal.event;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EventQueueConfiguration {
    public static final String LICENSE_TYPE_CHANGED_QUEUE = "LICENSE_TYPE_CHANGED_QUEUE";
    public static final String GIFT_SUBMITTED_QUEUE = "GIFT_SUBMITTED_QUEUE";

    @Bean
    public Queue licenseTypeChangedQueue() {
        return new Queue(LICENSE_TYPE_CHANGED_QUEUE, true);
    }

    @Bean
    public Queue giftSubmittedQueue() {
        return new Queue(GIFT_SUBMITTED_QUEUE, true);
    }

}
