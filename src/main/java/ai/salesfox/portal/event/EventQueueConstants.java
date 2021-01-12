package ai.salesfox.portal.event;

public class EventQueueConstants {
    // AMPQ Properties
    public static final String KEY_DEAD_LETTER_EXCHANGE = "x-dead-letter-exchange";
    public static final String KEY_DEAD_LETTER_EXCHANGE_ROUTING_KEY = "x-dead-letter-routing-key";
    public static final String KEY_SINGLE_ACTIVE_CONSUMER = "x-single-active-consumer";

    // Naming Suffixes
    public static final String QUEUE_SUFFIX = "_QUEUE";
    public static final String DLQ_SUFFIX = "_DEAD_LETTER_QUEUE";
    public static final String EXCHANGE_SUFFIX = "_EXCHANGE";

}
