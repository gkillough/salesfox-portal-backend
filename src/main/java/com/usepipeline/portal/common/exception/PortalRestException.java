package com.usepipeline.portal.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

public class PortalRestException extends Exception {
    @Getter
    private HttpStatus status;

    public PortalRestException(HttpStatus status) {
        super();
        this.status = status;
    }

    public PortalRestException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public PortalRestException(HttpStatus status, String message, Throwable cause) {
        super(message, cause);
        this.status = status;
    }

    public PortalRestException(HttpStatus status, Throwable cause) {
        super(cause);
        this.status = status;
    }

    protected PortalRestException(HttpStatus status, String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.status = status;
    }

}
