package ai.salesfox.portal.common.service.email;

import ai.salesfox.integration.common.exception.PortalException;

public class PortalEmailException extends PortalException {
    public PortalEmailException() {
        super();
    }

    public PortalEmailException(String message) {
        super(message);
    }

    public PortalEmailException(String message, Throwable cause) {
        super(message, cause);
    }

    public PortalEmailException(Throwable cause) {
        super(cause);
    }

}
