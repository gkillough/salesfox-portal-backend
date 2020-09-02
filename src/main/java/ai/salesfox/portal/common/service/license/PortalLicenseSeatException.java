package ai.salesfox.portal.common.service.license;

import ai.salesfox.integration.common.exception.SalesfoxException;

public class PortalLicenseSeatException extends SalesfoxException {
    public PortalLicenseSeatException() {
        super();
    }

    public PortalLicenseSeatException(String message) {
        super(message);
    }

    public PortalLicenseSeatException(String message, Throwable cause) {
        super(message, cause);
    }

    public PortalLicenseSeatException(Throwable cause) {
        super(cause);
    }

}
