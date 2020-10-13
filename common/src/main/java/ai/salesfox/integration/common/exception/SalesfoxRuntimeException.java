package ai.salesfox.integration.common.exception;

public class SalesfoxRuntimeException extends RuntimeException {
    public SalesfoxRuntimeException() {
    }

    public SalesfoxRuntimeException(String message) {
        super(message);
    }

    public SalesfoxRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public SalesfoxRuntimeException(Throwable cause) {
        super(cause);
    }

}
