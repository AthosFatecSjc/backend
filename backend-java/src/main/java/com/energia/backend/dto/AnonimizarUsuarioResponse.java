package com.energia.backend.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record AnonimizarUsuarioResponse(
        UUID usuarioId,
        String mensagem,
        LocalDateTime anonimizadoEm
) {}
