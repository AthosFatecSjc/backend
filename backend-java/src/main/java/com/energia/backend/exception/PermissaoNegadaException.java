package com.energia.backend.exception;

public class PermissaoNegadaException extends RuntimeException {

    public PermissaoNegadaException(String message) {
        super(message);
    }

    public PermissaoNegadaException(String message, Throwable cause) {
        super(message, cause);
    }
}
