package ai.salesfox.portal.integration.stripe;

import ai.salesfox.portal.common.exception.PortalException;
import ai.salesfox.portal.integration.stripe.configuration.StripeConfiguration;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Product;
import com.stripe.model.ProductCollection;
import com.stripe.param.ProductListParams;
import org.springframework.stereotype.Component;

@Component
public class StripeProductService {
    private final StripeConfiguration stripeConfiguration;

    public StripeProductService(StripeConfiguration stripeConfiguration) {
        this.stripeConfiguration = stripeConfiguration;
    }

    public ProductCollection requestProducts(boolean active, ProductListParams.Type productType) throws PortalException {
        Stripe.apiKey = stripeConfiguration.getStripeSecretKey();
        ProductListParams params = ProductListParams.builder()
                .setActive(true)
                .setType(productType)
                .build();
        try {
            return Product.list(params);
        } catch (StripeException e) {
            throw new PortalException("Failed to retrieve Products from Stripe", e);
        }
    }

    public Product requestProductById(String productId) throws PortalException {
        Stripe.apiKey = stripeConfiguration.getStripeSecretKey();
        try {
            return Product.retrieve(productId);
        } catch (StripeException e) {
            throw new PortalException("Failed to retrieve Product from Stripe", e);
        }
    }

    public void deleteProductById(String productId, boolean deleteAssociatedPrices) throws PortalException {
        Stripe.apiKey = stripeConfiguration.getStripeSecretKey();
        try {
            Product product = Product.retrieve(productId);
            if (deleteAssociatedPrices) {
                // TODO call StripePriceService to delete this product's prices (if any exist)
            }
            product.delete();
        } catch (StripeException e) {
            throw new PortalException("Failed to delete Product from Stripe", e);
        }
    }

}
