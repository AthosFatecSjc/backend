package com.energia.backend.dto;

import com.energia.backend.model.StatusUsuario;

import java.time.LocalDateTime;

public class MinhaContaResponse {
    private String nomeCompleto;
    private String email;
    private String telefone;
    private StatusUsuario status;
    private LocalDateTime dataCadastro;

    public MinhaContaResponse() {
    }

    public MinhaContaResponse(
            String nomeCompleto,
            String email,
            String telefone,
            StatusUsuario status,
            LocalDateTime dataCadastro
    ) {
        this.nomeCompleto = nomeCompleto;
        this.email = email;
        this.telefone = telefone;
        this.status = status;
        this.dataCadastro = dataCadastro;
    }

    public String getNomeCompleto() {
        return nomeCompleto;
    }

    public void setNomeCompleto(String nomeCompleto) {
        this.nomeCompleto = nomeCompleto;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public StatusUsuario getStatus() {
        return status;
    }

    public void setStatus(StatusUsuario status) {
        this.status = status;
    }

    public LocalDateTime getDataCadastro() {
        return dataCadastro;
    }

    public void setDataCadastro(LocalDateTime dataCadastro) {
        this.dataCadastro = dataCadastro;
    }
}
