package ai.salesfox.portal.common.exception;

import ai.salesfox.integration.common.exception.SalesfoxException;

public class PortalDatabaseIntegrityViolationException extends SalesfoxException {
    public PortalDatabaseIntegrityViolationException() {
        super();
    }

    public PortalDatabaseIntegrityViolationException(String message) {
        super(message);
    }

    public PortalDatabaseIntegrityViolationException(String message, Throwable cause) {
        super(message, cause);
    }

    public PortalDatabaseIntegrityViolationException(Throwable cause) {
        super(cause);
    }

}
