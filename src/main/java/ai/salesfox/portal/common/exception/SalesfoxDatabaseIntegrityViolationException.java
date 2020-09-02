package ai.salesfox.portal.common.exception;

import ai.salesfox.integration.common.exception.SalesfoxException;

public class SalesfoxDatabaseIntegrityViolationException extends SalesfoxException {
    public SalesfoxDatabaseIntegrityViolationException() {
        super();
    }

    public SalesfoxDatabaseIntegrityViolationException(String message) {
        super(message);
    }

    public SalesfoxDatabaseIntegrityViolationException(String message, Throwable cause) {
        super(message, cause);
    }

    public SalesfoxDatabaseIntegrityViolationException(Throwable cause) {
        super(cause);
    }

}
