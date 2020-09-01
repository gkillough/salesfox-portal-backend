package ai.salesfox.portal.common.exception;

import ai.salesfox.integration.common.exception.PortalException;

public class PortalFileSystemException extends PortalException {
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
