package ai.salesfox.portal.common.exception;

import ai.salesfox.integration.common.exception.SalesfoxException;

public class PortalException extends SalesfoxException {
    public PortalException() {
    }

    public PortalException(String message) {
        super(message);
    }

    public PortalException(String message, Throwable cause) {
        super(message, cause);
    }

    public PortalException(Throwable cause) {
        super(cause);
    }

}
