package com.energia.backend.dto;

import java.util.List;
import java.util.UUID;

public record UsuarioHistoricoTermosResponse(
    UUID userId,
    List<TermoPeriodoResponse> termos
) {
}