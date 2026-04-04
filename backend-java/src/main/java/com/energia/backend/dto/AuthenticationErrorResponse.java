package com.energia.backend.dto;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;

public class AuthenticationErrorResponse {
    private String timestamp;
    private int status;
    private String code;
    private String message;
    private String severity;
    private String reason;

    public AuthenticationErrorResponse() {
    }

    public AuthenticationErrorResponse(int status, String code, String message, String severity) {
        this.timestamp = ZonedDateTime.now().toString();
        this.status = status;
        this.code = code;
        this.message = message;
        this.severity = severity;
    }

    public AuthenticationErrorResponse(int status, String code, String message, String severity, String reason) {
        this.timestamp = ZonedDateTime.now().toString();
        this.status = status;
        this.code = code;
        this.message = message;
        this.severity = severity;
        this.reason = reason;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
