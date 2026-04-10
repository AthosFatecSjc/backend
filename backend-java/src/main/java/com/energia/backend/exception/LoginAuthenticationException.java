package com.energia.backend.exception;

public class LoginAuthenticationException extends RuntimeException {
    private String errorCode;
    private int httpStatus;
    private String reason;

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

    public String getErrorCode() {
        return errorCode;
    }

    public int getHttpStatus() {
        return httpStatus;
    }

    public String getReason() {
        return reason;
    }
}
