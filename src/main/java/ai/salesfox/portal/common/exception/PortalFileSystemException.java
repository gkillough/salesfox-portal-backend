package ai.salesfox.portal.common.exception;

import ai.salesfox.integration.common.exception.SalesfoxException;

public class PortalFileSystemException extends SalesfoxException {
    public PortalFileSystemException() {
        super();
    }

    public PortalFileSystemException(String message) {
        super(message);
    }

    public PortalFileSystemException(String message, Throwable cause) {
        super(message, cause);
    }

    public PortalFileSystemException(Throwable cause) {
        super(cause);
    }

}
