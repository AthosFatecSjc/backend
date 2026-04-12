package com.energia.backend.exception;

import java.util.Map;

public class LoginAuthenticationException extends RuntimeException {
    private String errorCode;
    private int httpStatus;
    private String reason;
    private Map<String, Object> details;

    public LoginAuthenticationException(String message, String errorCode, int httpStatus) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }

    public LoginAuthenticationException(String message, String errorCode, int httpStatus, String reason) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
        this.reason = reason;
    }

    public LoginAuthenticationException(
            String message,
            String errorCode,
            int httpStatus,
            String reason,
            Map<String, Object> details
    ) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
        this.reason = reason;
        this.details = details;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public int getHttpStatus() {
        return httpStatus;
    }

    public String getReason() {
        return reason;
    }

    public Map<String, Object> getDetails() {
        return details;
    }
}
