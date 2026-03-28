package com.energia.backend.dto;

import java.util.UUID;

public class AprovacaoRejeicaoUsuarioRequest {
    private UUID usuarioId;
    private String motivo; 

    public UUID getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(UUID usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }
}