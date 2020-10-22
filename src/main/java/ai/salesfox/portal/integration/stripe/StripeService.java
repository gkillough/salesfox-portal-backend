package ai.salesfox.portal.integration.stripe;

import com.stripe.Stripe;
import com.stripe.model.Charge;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class StripeService {

    @Value("${stripe.key.secret}")
    private String API_SECRET_KEY;

    @Autowired
    public StripeService() {
        Stripe.apiKey = API_SECRET_KEY;
    }

    public Charge chargeNewCard(String token, double amount) throws Exception {
        try {
            Map<String, Object> chargeParams = new HashMap<>();
            chargeParams.put("amount", (int) (amount * 100));
            chargeParams.put("currency", "USD");
            chargeParams.put("source", token);
            Charge charge = Charge.create(chargeParams);
            return charge;
        } catch (Exception e) {
            throw new Exception();//construct MyException however; this is just an example
        }
    }

}
