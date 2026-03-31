package com.energia.backend.controller;

import com.energia.backend.exception.DocumentosObrigatoriosNaoConfiguradosException;
import com.energia.backend.exception.EmailJaCadastradoException;
import com.energia.backend.exception.UsuarioNaoEncontradoException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

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

    @ExceptionHandler(UsuarioNaoEncontradoException.class)
    public ResponseEntity<Map<String, String>> handleUsuarioNaoEncontrado(UsuarioNaoEncontradoException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                "erro", "USUARIO_NAO_ENCONTRADO",
                "mensagem", ex.getMessage()
        ));
    }

    @ExceptionHandler(DocumentosObrigatoriosNaoConfiguradosException.class)
    public ResponseEntity<Map<String, String>> handleDocumentosObrigatoriosNaoConfigurados(
            DocumentosObrigatoriosNaoConfiguradosException ex
    ) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of(
                "erro", "DOCUMENTOS_CONSENTIMENTO_INDISPONIVEIS",
                "mensagem", ex.getMessage()
        ));
    }
}
