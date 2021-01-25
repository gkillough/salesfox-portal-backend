package ai.salesfox.portal.integration.stripe.service;

import ai.salesfox.portal.common.exception.PortalException;
import ai.salesfox.portal.integration.stripe.configuration.StripeConfiguration;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Subscription;
import com.stripe.model.SubscriptionCollection;
import com.stripe.param.SubscriptionCreateParams;
import com.stripe.param.SubscriptionListParams;
import com.stripe.param.SubscriptionUpdateParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StripeSubscriptionService {
    private final StripeConfiguration stripeConfiguration;

    @Autowired
    public StripeSubscriptionService(StripeConfiguration stripeConfiguration) {
        this.stripeConfiguration = stripeConfiguration;
    }

    public Subscription retrieveSubscription(String subscriptionId) throws PortalException {
        Stripe.apiKey = stripeConfiguration.getStripeSecretKey();
        try {
            return Subscription.retrieve(subscriptionId);
        } catch (StripeException e) {
            throw new PortalException("Failed to retrieve Subscription from Stripe", e);
        }
    }

    public SubscriptionCollection retrieveSubscriptions(String customerId) throws PortalException {
        Stripe.apiKey = stripeConfiguration.getStripeSecretKey();
        SubscriptionListParams params = SubscriptionListParams.builder()
                .setCustomer(customerId)
                .build();
        try {
            return Subscription.list(params);
        } catch (StripeException e) {
            throw new PortalException("Failed to retrieve Subscriptions for the specified Customer from Stripe", e);
        }
    }

    public Subscription createSubscription(SubscriptionCreateParams subscriptionCreateParams) throws PortalException {
        Stripe.apiKey = stripeConfiguration.getStripeSecretKey();
        try {
            return Subscription.create(subscriptionCreateParams);
        } catch (StripeException e) {
            throw new PortalException("Failed to create Subscription in Stripe", e);
        }
    }

    public void updateSubscriptionById(String subscriptionId, SubscriptionUpdateParams subscriptionUpdateParams) throws PortalException {
        Stripe.apiKey = stripeConfiguration.getStripeSecretKey();
        Subscription subscription = retrieveSubscription(subscriptionId);
        try {
            subscription.update(subscriptionUpdateParams);
        } catch (StripeException e) {
            throw new PortalException("Failed to create Subscription in Stripe", e);
        }
    }

    public void cancelSubscriptionById(String subscriptionId) throws PortalException {
        Stripe.apiKey = stripeConfiguration.getStripeSecretKey();
        SubscriptionUpdateParams params = SubscriptionUpdateParams.builder()
                .setCancelAtPeriodEnd(true)
                .build();
        updateSubscriptionById(subscriptionId, params);
    }

}
