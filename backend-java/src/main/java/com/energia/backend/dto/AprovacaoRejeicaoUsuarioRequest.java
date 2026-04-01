package com.energia.backend.dto;

import com.energia.backend.model.StatusUsuario;

public class AprovacaoRejeicaoUsuarioRequest {
    private StatusUsuario status;
    private String motivo; 

    public StatusUsuario getStatus() {
        return status;
    }

    public void setStatus(StatusUsuario status) {
        this.status = status;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }
}