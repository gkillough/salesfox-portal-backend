package ai.salesfox.portal.common.exception;

import ai.salesfox.integration.common.exception.SalesfoxRuntimeException;

public class PortalRuntimeException extends SalesfoxRuntimeException {
    public PortalRuntimeException() {
    }

    public PortalRuntimeException(String message) {
        super(message);
    }

    public PortalRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public PortalRuntimeException(Throwable cause) {
        super(cause);
    }

}
