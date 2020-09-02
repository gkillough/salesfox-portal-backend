package ai.salesfox.portal.common.service.email;

import ai.salesfox.integration.common.exception.SalesfoxException;

public class SalesfoxEmailException extends SalesfoxException {
    public SalesfoxEmailException() {
        super();
    }

    public SalesfoxEmailException(String message) {
        super(message);
    }

    public SalesfoxEmailException(String message, Throwable cause) {
        super(message, cause);
    }

    public SalesfoxEmailException(Throwable cause) {
        super(cause);
    }

}
