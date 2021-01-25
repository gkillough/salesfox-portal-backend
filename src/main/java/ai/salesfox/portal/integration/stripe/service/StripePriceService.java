package ai.salesfox.portal.integration.stripe.service;

import ai.salesfox.portal.common.exception.PortalException;
import ai.salesfox.portal.integration.stripe.configuration.StripeConfiguration;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Price;
import com.stripe.model.PriceCollection;
import com.stripe.param.PriceCreateParams;
import com.stripe.param.PriceListParams;
import com.stripe.param.PriceUpdateParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StripePriceService {
    private final StripeConfiguration stripeConfiguration;

    @Autowired
    public StripePriceService(StripeConfiguration stripeConfiguration) {
        this.stripeConfiguration = stripeConfiguration;
    }

    public PriceCollection retrievePrices(String productId, boolean active, PriceListParams.Type type) throws PortalException {
        Stripe.apiKey = stripeConfiguration.getStripeSecretKey();
        PriceListParams params = PriceListParams.builder()
                .setProduct(productId)
                .setActive(active)
                .setType(type)
                .build();

        try {
            return Price.list(params);
        } catch (StripeException e) {
            throw new PortalException("Failed to retrieve Prices for the specified Product from Stripe", e);
        }
    }

    public Price retrievePriceById(String priceId) throws PortalException {
        Stripe.apiKey = stripeConfiguration.getStripeSecretKey();
        try {
            return Price.retrieve(priceId);
        } catch (StripeException e) {
            throw new PortalException("Failed to retrieve Price from Stripe", e);
        }
    }

    public Price createPrice(PriceCreateParams priceCreateParams) throws PortalException {
        Stripe.apiKey = stripeConfiguration.getStripeSecretKey();
        try {
            return Price.create(priceCreateParams);
        } catch (StripeException e) {
            throw new PortalException("Failed to create Price in Stripe", e);
        }
    }

    public void updatePriceById(String priceId, PriceUpdateParams priceUpdateParams) throws PortalException {
        Stripe.apiKey = stripeConfiguration.getStripeSecretKey();
        Price price = retrievePriceById(priceId);
        try {
            price.update(priceUpdateParams);
        } catch (StripeException e) {
            throw new PortalException("Failed to update Price in Stripe", e);
        }
    }

}
