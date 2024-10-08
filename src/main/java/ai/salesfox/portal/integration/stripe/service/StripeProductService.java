package ai.salesfox.portal.integration.stripe.service;

import ai.salesfox.portal.common.exception.PortalException;
import ai.salesfox.portal.integration.stripe.configuration.StripeConfiguration;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Product;
import com.stripe.model.ProductCollection;
import com.stripe.param.ProductCreateParams;
import com.stripe.param.ProductListParams;
import com.stripe.param.ProductUpdateParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StripeProductService {
    private final StripeConfiguration stripeConfiguration;

    @Autowired
    public StripeProductService(StripeConfiguration stripeConfiguration) {
        this.stripeConfiguration = stripeConfiguration;
    }

    public ProductCollection requestProducts(boolean active, ProductListParams.Type productType) throws PortalException {
        Stripe.apiKey = stripeConfiguration.getStripeSecretKey();
        ProductListParams params = ProductListParams.builder()
                .setActive(active)
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

    public Product createProduct(ProductCreateParams productCreateParams) throws PortalException {
        Stripe.apiKey = stripeConfiguration.getStripeSecretKey();
        try {
            return Product.create(productCreateParams);
        } catch (StripeException e) {
            throw new PortalException("Failed to create Product in Stripe", e);
        }
    }

    public void updateProductById(String productId, ProductUpdateParams productUpdateParams) throws PortalException {
        Stripe.apiKey = stripeConfiguration.getStripeSecretKey();
        Product product = requestProductById(productId);

        try {
            product.update(productUpdateParams);
        } catch (StripeException e) {
            throw new PortalException("Failed to update Product in Stripe", e);
        }
    }

}
