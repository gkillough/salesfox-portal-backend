package ai.salesfox.portal.integration.stripe;

import ai.salesfox.portal.common.model.PortalAddressModel;
import com.stripe.param.CustomerCreateParams;

public final class StripeAddressUtils {
    public static CustomerCreateParams.Address toCustomerCreateParamsAddress(PortalAddressModel portalAddress) {
        return CustomerCreateParams.Address.builder()
                .setLine1(portalAddress.getAddressLine1())
                .setLine2(portalAddress.getAddressLine2())
                .setCity(portalAddress.getCity())
                .setState(portalAddress.getState())
                .setPostalCode(portalAddress.getZipCode())
                .build();
    }

}
