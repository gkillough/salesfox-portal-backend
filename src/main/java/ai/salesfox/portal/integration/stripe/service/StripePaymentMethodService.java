package ai.salesfox.portal.integration.stripe.service;

import ai.salesfox.portal.common.exception.PortalException;
import ai.salesfox.portal.integration.stripe.configuration.StripeConfiguration;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentMethod;
import com.stripe.param.CustomerUpdateParams;
import com.stripe.param.PaymentMethodAttachParams;
import com.stripe.param.PaymentMethodCreateParams;
import com.stripe.param.PaymentMethodListParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class StripePaymentMethodService {
    private final StripeConfiguration stripeConfiguration;
    private final StripeCustomerService stripeCustomerService;

    @Autowired
    public StripePaymentMethodService(StripeConfiguration stripeConfiguration, StripeCustomerService stripeCustomerService) {
        this.stripeConfiguration = stripeConfiguration;
        this.stripeCustomerService = stripeCustomerService;
    }

    public Optional<PaymentMethod> retrieveCustomerPaymentMethod(String customerId) throws PortalException {
        Stripe.apiKey = stripeConfiguration.getStripeSecretKey();
        PaymentMethodListParams params = PaymentMethodListParams.builder()
                .setCustomer(customerId)
                .build();
        try {
            return PaymentMethod.list(params).getData()
                    .stream()
                    .findAny();
        } catch (StripeException e) {
            throw new PortalException("Failed to retrieve PaymentMethod from Stripe", e);
        }
    }

    public PaymentMethod attachCustomerPaymentMethod(String customerId, PaymentMethodCreateParams paymentMethodCreateParams) throws PortalException {
        Stripe.apiKey = stripeConfiguration.getStripeSecretKey();

        PaymentMethod paymentMethod;
        try {
            paymentMethod = PaymentMethod.create(paymentMethodCreateParams);
        } catch (StripeException e) {
            throw new PortalException("Failed to create PaymentMethod in Stripe", e);
        }

        PaymentMethodAttachParams paymentMethodAttachParams = PaymentMethodAttachParams.builder()
                .setCustomer(customerId)
                .build();
        try {
            paymentMethod.attach(paymentMethodAttachParams);
        } catch (StripeException e) {
            throw new PortalException("Failed to attach PaymentMethod to Customer in Stripe", e);
        }

        CustomerUpdateParams updateCustomerParams = CustomerUpdateParams.builder()
                // TODO verify that this is the correct parameter
                .setDefaultSource(paymentMethod.getId())
                .build();
        stripeCustomerService.updateCustomerById(customerId, updateCustomerParams);

        return paymentMethod;
    }

    public void detachCustomerPaymentMethod(String customerId) throws PortalException {
        Stripe.apiKey = stripeConfiguration.getStripeSecretKey();

        Optional<PaymentMethod> optionalPaymentMethod = retrieveCustomerPaymentMethod(customerId);
        if (optionalPaymentMethod.isPresent()) {
            PaymentMethod paymentMethod = optionalPaymentMethod.get();
            try {
                paymentMethod.detach();
            } catch (StripeException e) {
                throw new PortalException("Failed to detach PaymentMethod from Customer in Stripe", e);
            }
        }
    }

}
