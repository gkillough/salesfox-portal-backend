package ai.salesfox.portal.common.service.license;

import ai.salesfox.integration.common.exception.SalesfoxException;

public class SalesfoxLicenseSeatException extends SalesfoxException {
    public SalesfoxLicenseSeatException() {
        super();
    }

    public SalesfoxLicenseSeatException(String message) {
        super(message);
    }

    public SalesfoxLicenseSeatException(String message, Throwable cause) {
        super(message, cause);
    }

    public SalesfoxLicenseSeatException(Throwable cause) {
        super(cause);
    }

}
