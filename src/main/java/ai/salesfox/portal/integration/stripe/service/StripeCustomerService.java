package ai.salesfox.portal.integration.stripe.service;

import ai.salesfox.portal.common.exception.PortalException;
import ai.salesfox.portal.common.model.PortalAddressModel;
import ai.salesfox.portal.integration.stripe.StripeAddressUtils;
import ai.salesfox.portal.integration.stripe.configuration.StripeConfiguration;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.CustomerListParams;
import com.stripe.param.CustomerUpdateParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class StripeCustomerService {
    private final StripeConfiguration stripeConfiguration;

    @Autowired
    public StripeCustomerService(StripeConfiguration stripeConfiguration) {
        this.stripeConfiguration = stripeConfiguration;
    }

    public Optional<Customer> findCustomerByEmailAddress(String emailAddress) throws PortalException {
        Stripe.apiKey = stripeConfiguration.getStripeSecretKey();
        CustomerListParams params = CustomerListParams.builder()
                .setEmail(emailAddress)
                .build();
        try {
            return Customer.list(params).getData()
                    .stream()
                    .findAny();
        } catch (StripeException e) {
            throw new PortalException("Failed to retrieve Customer from Stripe", e);
        }
    }

    public Customer retrieveCustomerById(String customerId) throws PortalException {
        Stripe.apiKey = stripeConfiguration.getStripeSecretKey();
        try {
            return Customer.retrieve(customerId);
        } catch (StripeException e) {
            throw new PortalException("Failed to retrieve Customer from Stripe", e);
        }
    }

    public Customer createCustomer(String name, String emailAddress, PortalAddressModel address) throws PortalException {
        Stripe.apiKey = stripeConfiguration.getStripeSecretKey();

        CustomerCreateParams.Address stripeAddress = StripeAddressUtils.toCustomerCreateParamsAddress(address);
        CustomerCreateParams params = CustomerCreateParams.builder()
                .setName(name)
                .setEmail(emailAddress)
                .setAddress(stripeAddress)
                .build();

        return createCustomer(params);
    }

    public Customer createCustomer(CustomerCreateParams customerCreateParams) throws PortalException {
        Stripe.apiKey = stripeConfiguration.getStripeSecretKey();
        try {
            return Customer.create(customerCreateParams);
        } catch (StripeException e) {
            throw new PortalException("Failed to create Customer in Stripe", e);
        }
    }

    public void updateCustomerById(String customerId, CustomerUpdateParams customerUpdateParams) throws PortalException {
        Stripe.apiKey = stripeConfiguration.getStripeSecretKey();

        Customer customer = retrieveCustomerById(customerId);
        updateCustomer(customer, customerUpdateParams);
    }

    public void updateCustomer(Customer customer, CustomerUpdateParams customerUpdateParams) throws PortalException {
        Stripe.apiKey = stripeConfiguration.getStripeSecretKey();
        try {
            customer.update(customerUpdateParams);
        } catch (StripeException e) {
            throw new PortalException("Failed to create Customer in Stripe", e);
        }
    }

}
