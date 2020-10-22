package ai.salesfox.portal.integration.stripe;

import ai.salesfox.portal.common.exception.PortalException;
import ai.salesfox.portal.integration.stripe.configuration.StripeConfiguration;
import com.stripe.Stripe;
import com.stripe.model.Charge;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class StripeService {

    private final StripeConfiguration stripeConfiguration;

    @Autowired
    public StripeService(StripeConfiguration stripConfiguration) {
        this.stripeConfiguration = stripConfiguration;
        Stripe.apiKey = stripeConfiguration.getStripeSecretKey();
    }

    public Charge chargeNewCard(String token, double amount) throws PortalException {
        try {
            Map<String, Object> chargeParams = new HashMap<>();
            chargeParams.put("amount", (int) (amount * 100));
            chargeParams.put("currency", "USD");
            chargeParams.put("source", token);
            Charge charge = Charge.create(chargeParams);
            return charge;
        } catch (Exception e) {
            throw new PortalException("Stripe was unable to charge this payment method");
        }
    }

}
