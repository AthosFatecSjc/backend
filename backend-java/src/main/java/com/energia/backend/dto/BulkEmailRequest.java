package com.energia.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class BulkEmailRequest {

    @NotBlank(message = "Assunto e obrigatorio.")
    @Size(max = 150, message = "Assunto deve ter no maximo 150 caracteres.")
    private String subject;

    @NotBlank(message = "Mensagem e obrigatoria.")
    @Size(max = 10000, message = "Mensagem deve ter no maximo 10000 caracteres.")
    private String body;

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
