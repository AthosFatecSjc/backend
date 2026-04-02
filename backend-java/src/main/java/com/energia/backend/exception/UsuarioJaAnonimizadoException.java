package com.energia.backend.exception;

public class UsuarioJaAnonimizadoException extends RuntimeException {

    public UsuarioJaAnonimizadoException(String message) {
        super(message);
    }

    public UsuarioJaAnonimizadoException(String message, Throwable cause) {
        super(message, cause);
    }
}
