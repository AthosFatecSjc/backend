package com.energia.backend.dto;

import com.energia.backend.model.StatusUsuario;
import java.util.UUID;

public class AprovacaoRejeicaoUsuarioRequest {
    private StatusUsuario status;
    private String motivo; 
    private UUID usuarioId;

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

    public UUID getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(UUID usuarioId) {
        this.usuarioId = usuarioId;
    }
}