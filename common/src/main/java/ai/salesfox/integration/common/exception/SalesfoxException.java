package ai.salesfox.integration.common.exception;

public class SalesfoxException extends Exception {
    public SalesfoxException() {
        super();
    }

    public SalesfoxException(String message) {
        super(message);
    }

    public SalesfoxException(String message, Throwable cause) {
        super(message, cause);
    }

    public SalesfoxException(Throwable cause) {
        super(cause);
    }

}
