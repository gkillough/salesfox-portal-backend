package ai.salesfox.integration.common.exception;

import lombok.Getter;

public class SalesfoxHttpException extends SalesfoxException {
    @Getter
    private final int httpStatusCode;
    @Getter
    private final String httpStatusMessage;

    public SalesfoxHttpException(int httpStatusCode, String httpStatusMessage) {
        super(httpStatusMessage);
        this.httpStatusCode = httpStatusCode;
        this.httpStatusMessage = httpStatusMessage;
    }

    public SalesfoxHttpException(int httpStatusCode, String httpStatusMessage, Throwable throwable) {
        super(httpStatusMessage, throwable);
        this.httpStatusCode = httpStatusCode;
        this.httpStatusMessage = httpStatusMessage;
    }

}
