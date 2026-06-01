package com.energia.backend.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.energia.backend.exception.DocumentosObrigatoriosNaoConfiguradosException;
import com.energia.backend.exception.EmailJaCadastradoException;
import com.energia.backend.exception.LoginAuthenticationException;
import com.energia.backend.exception.PermissaoNegadaException;
import com.energia.backend.exception.TermoNaoEncontradoException;
import com.energia.backend.exception.UsuarioJaDeletadoException;
import com.energia.backend.exception.UsuarioNaoEncontradoException;
import com.energia.backend.dto.error.ErrorResponseDto;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EmailJaCadastradoException.class)
    public ResponseEntity<ErrorResponseDto> handleEmailDuplicado(EmailJaCadastradoException ex) {
        return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(ErrorResponseDto.builder()
                .error("EMAIL_DUPLICADO")
                .message(ex.getMessage())
                .build()
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponseDto> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponseDto.builder()  
                .error("DADOS_INVALIDOS")
                .message(ex.getMessage())
                .build()
            );
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponseDto> handleInvalidState(IllegalStateException ex) {
        return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(ErrorResponseDto.builder()
                .error("OPERACAO_INVALIDA")
                .message(ex.getMessage())
                .build()
            );
    }

    @ExceptionHandler(TermoNaoEncontradoException.class)
    public ResponseEntity<ErrorResponseDto> handleTermoNaoEncontrado(TermoNaoEncontradoException ex) {
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponseDto.builder()
                .error("TERMO_NAO_ENCONTRADO")
                .message(ex.getMessage())
                .build()
            );
    }

    @ExceptionHandler(UsuarioNaoEncontradoException.class)
    public ResponseEntity<ErrorResponseDto> handleUsuarioNaoEncontrado(UsuarioNaoEncontradoException ex) {
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(ErrorResponseDto.builder()
                .error("USUARIO_NAO_ENCONTRADO")
                .message(ex.getMessage())
                .build()
            );
    }

    @ExceptionHandler(DocumentosObrigatoriosNaoConfiguradosException.class)
    public ResponseEntity<ErrorResponseDto> handleDocumentosObrigatoriosNaoConfigurados(
            DocumentosObrigatoriosNaoConfiguradosException ex
    ) {
        return ResponseEntity
            .status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(ErrorResponseDto.builder()
                .error("DOCUMENTOS_CONSENTIMENTO_INDISPONIVEIS")
                .message(ex.getMessage())
                .build()
            );
    }

    @ExceptionHandler(PermissaoNegadaException.class)
    public ResponseEntity<ErrorResponseDto> handlePermissaoNegada(PermissaoNegadaException ex) {
        return ResponseEntity
            .status(HttpStatus.FORBIDDEN)
            .body(ErrorResponseDto.builder()
                .error("PERMISSAO_NEGADA")
                .message(ex.getMessage())
                .build()
        );
    }

    @ExceptionHandler(UsuarioJaDeletadoException.class)
    public ResponseEntity<ErrorResponseDto> handleUsuarioJaDeletado(UsuarioJaDeletadoException ex) {
        return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(ErrorResponseDto.builder()
                .error("USUARIO_JA_DELETADO")
                .message(ex.getMessage())
                .build()
            );
    }

    @ExceptionHandler(LoginAuthenticationException.class)
    public ResponseEntity<Map<String, Object>> handleLoginAuthenticationException(LoginAuthenticationException ex) {
        Map<String, Object> response = Map.of(
                "erro", ex.getErrorCode(),
                "mensagem", ex.getMessage(),
                "status", ex.getHttpStatus()
        );
        HttpStatus httpStatus = HttpStatus.valueOf(ex.getHttpStatus());
        return ResponseEntity.status(httpStatus).body(response);
    }
}
