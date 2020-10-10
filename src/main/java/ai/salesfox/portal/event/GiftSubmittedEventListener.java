package ai.salesfox.portal.event;

import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class GiftSubmittedEventListener implements ApplicationListener<GiftSubmittedEvent> {

    @Override
    public void onApplicationEvent(GiftSubmittedEvent event) {

    }

}
