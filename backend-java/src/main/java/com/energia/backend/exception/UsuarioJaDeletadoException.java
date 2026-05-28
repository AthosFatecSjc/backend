package com.energia.backend.exception;

public class UsuarioJaDeletadoException extends RuntimeException {

    public UsuarioJaDeletadoException(String message) {
        super(message);
    }

    public UsuarioJaDeletadoException(String message, Throwable cause) {
        super(message, cause);
    }
}
