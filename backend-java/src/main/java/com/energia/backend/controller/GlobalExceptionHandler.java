package com.energia.backend.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.energia.backend.exception.EmailJaCadastradoException;
import com.energia.backend.exception.PermissaoNegadaException;
import com.energia.backend.exception.TermoNaoEncontradoException;
import com.energia.backend.exception.UsuarioJaAnonimizadoException;
import com.energia.backend.exception.UsuarioNaoEncontradoException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EmailJaCadastradoException.class)
    public ResponseEntity<Map<String, String>> handleEmailDuplicado(EmailJaCadastradoException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                "erro", "EMAIL_DUPLICADO",
                "mensagem", ex.getMessage()
        ));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "erro", "DADOS_INVALIDOS",
                "mensagem", ex.getMessage()
        ));
    }

    @ExceptionHandler(TermoNaoEncontradoException.class)
    public ResponseEntity<Map<String, String>> handleTermoNaoEncontrado(TermoNaoEncontradoException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "erro", "TERMO_NAO_ENCONTRADO",
                "mensagem", ex.getMessage()
        ));
    }

    @ExceptionHandler(UsuarioNaoEncontradoException.class)
    public ResponseEntity<Map<String, String>> handleUsuarioNaoEncontrado(UsuarioNaoEncontradoException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                "erro", "USUARIO_NAO_ENCONTRADO",
                "mensagem", ex.getMessage()
        ));
    }

    @ExceptionHandler(PermissaoNegadaException.class)
    public ResponseEntity<Map<String, String>> handlePermissaoNegada(PermissaoNegadaException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                "erro", "PERMISSAO_NEGADA",
                "mensagem", ex.getMessage()
        ));
    }

    @ExceptionHandler(UsuarioJaAnonimizadoException.class)
    public ResponseEntity<Map<String, String>> handleUsuarioJaAnonimizado(UsuarioJaAnonimizadoException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                "erro", "USUARIO_JA_ANONIMIZADO",
                "mensagem", ex.getMessage()
        ));
    }
}
