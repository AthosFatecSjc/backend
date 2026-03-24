package com.energia.backend.dto;

import com.energia.backend.model.StatusUsuario;

public class UsuarioCadastroResponse {
    private String mensagem;
    private String email;
    private StatusUsuario status;

    public UsuarioCadastroResponse(String mensagem, String email, StatusUsuario status) {
        this.mensagem = mensagem;
        this.email = email;
        this.status = status;
    }

    public String getMensagem() {
        return mensagem;
    }

    public String getEmail() {
        return email;
    }

    public StatusUsuario getStatus() {
        return status;
    }
}
