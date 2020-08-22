package ai.salesfox.portal.common.service.license;

import ai.salesfox.portal.common.exception.PortalException;

public class PortalLicenseSeatException extends PortalException {
    public PortalLicenseSeatException() {
        super();
    }

    public PortalLicenseSeatException(String message) {
        super(message);
    }

    public PortalLicenseSeatException(String message, Throwable cause) {
        super(message, cause);
    }

    public PortalLicenseSeatException(Throwable cause) {
        super(cause);
    }

}
