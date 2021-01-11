package ai.salesfox.portal.integration.stripe;

import ai.salesfox.portal.common.exception.PortalException;
import ai.salesfox.portal.integration.stripe.configuration.StripeConfiguration;
import com.stripe.Stripe;
import com.stripe.model.Charge;
import com.stripe.param.ChargeCreateParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StripeChargeService {
    private final StripeConfiguration stripeConfiguration;

    @Autowired
    public StripeChargeService(StripeConfiguration stripConfiguration) {
        this.stripeConfiguration = stripConfiguration;
        Stripe.apiKey = stripeConfiguration.getStripeSecretKey();
    }

    public Charge chargeNewCard(String chargeToken, double amount, String description, String receiptEmailAddress) throws PortalException {
        try {
            long amountInCents = (long) amount * 100;
            ChargeCreateParams chargeParams = ChargeCreateParams.builder()
                    .setAmount(amountInCents)
                    .setCurrency("USD")
                    .setSource(chargeToken)
                    .setDescription(description)
                    .setStatementDescriptorSuffix("Salesfox Order")
                    .setReceiptEmail(receiptEmailAddress)
                    .build();
            return Charge.create(chargeParams);
        } catch (Exception e) {
            throw new PortalException("Stripe was unable to charge this payment method: " + e.getMessage());
        }
    }

}
