package ai.salesfox.portal.common.service.email;

public class PortalEmailException extends Exception {
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
