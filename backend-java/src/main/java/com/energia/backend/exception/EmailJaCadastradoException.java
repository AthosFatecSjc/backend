package com.energia.backend.exception;

public class EmailJaCadastradoException extends RuntimeException {
    public EmailJaCadastradoException(String email) {
        super("This email address is already registered (" + email + ")");
    }
}
