package ai.salesfox.portal.common.exception;

import ai.salesfox.integration.common.exception.SalesfoxException;

public class SalesfoxFileSystemException extends SalesfoxException {
    public SalesfoxFileSystemException() {
        super();
    }

    public SalesfoxFileSystemException(String message) {
        super(message);
    }

    public SalesfoxFileSystemException(String message, Throwable cause) {
        super(message, cause);
    }

    public SalesfoxFileSystemException(Throwable cause) {
        super(cause);
    }

}
