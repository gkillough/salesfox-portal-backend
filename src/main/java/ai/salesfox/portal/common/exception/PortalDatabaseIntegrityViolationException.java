package ai.salesfox.portal.common.exception;

import ai.salesfox.integration.common.exception.PortalException;

public class PortalDatabaseIntegrityViolationException extends PortalException {
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
